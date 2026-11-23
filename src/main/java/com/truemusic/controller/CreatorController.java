package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.truemusic.common.Result;
import com.truemusic.entity.Song;
import com.truemusic.entity.User;
import com.truemusic.mapper.SongMapper;
import com.truemusic.mapper.UserMapper;
import com.truemusic.service.FileService;
import com.truemusic.vo.SongVO;
import com.truemusic.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final UserMapper userMapper;
    private final SongMapper songMapper;
    private final FileService fileService;

    /**
     * 1. 获取创作者（音乐人）列表
     * 按照上传歌曲数量倒序排列，或者直接按注册时间分页
     */
    @GetMapping
    public Result<?> getCreators(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 分页查询所有用户
        Page<User> p = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));

        // 组装结果
        List<Map<String, Object>> records = p.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("nickname", user.getNickname());
            map.put("avatarUrl", fileService.getPresignedUrl(user.getAvatarUrl()));

            // 统计该用户上传了多少首歌 (这里存在N+1查询问题，但早期数据量小可忽略)
            Long songCount = songMapper.selectCount(new LambdaQueryWrapper<Song>()
                    .eq(Song::getUploaderId, user.getId()));
            map.put("songCount", songCount);

            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent());
        data.put("size", p.getSize());

        return Result.ok(data);
    }

    /**
     * 2. 获取指定创作者的公开主页信息
     */
    @GetMapping("/{id}")
    public Result<?> getCreatorInfo(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "创作者不存在");
        }

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(fileService.getPresignedUrl(user.getAvatarUrl()));

        return Result.ok(vo);
    }

    /**
     * 3. 获取指定创作者的所有歌曲 (带详情及预签名 URL)
     */
    @GetMapping("/{id}/songs")
    public Result<?> getCreatorSongs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 查询该用户上传的所有歌曲
        Page<Song> p = songMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Song>()
                        .eq(Song::getUploaderId, id)
                        .orderByDesc(Song::getCreatedAt));

        // 转换为 VO
        List<SongVO> records = p.getRecords().stream().map(song -> {
            SongVO vo = new SongVO();
            vo.setId(song.getId());
            vo.setTitle(song.getTitle());
            vo.setArtist(song.getArtist());
            vo.setLyric(song.getLyric());
            vo.setLikeCount(song.getLikeCount());
            vo.setAudioUrl(fileService.getPresignedUrl(song.getAudioKey()));
            vo.setCoverUrl(fileService.getPresignedUrl(song.getCoverKey()));
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent());
        data.put("size", p.getSize());

        return Result.ok(data);
    }
}