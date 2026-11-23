package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.truemusic.common.Result;
import com.truemusic.common.UserContext;
import com.truemusic.entity.Playlist;
import com.truemusic.entity.Song;
import com.truemusic.entity.SongFavorite;
import com.truemusic.entity.User;
import com.truemusic.mapper.PlaylistMapper;
import com.truemusic.mapper.SongFavoriteMapper;
import com.truemusic.mapper.SongMapper;
import com.truemusic.mapper.UserMapper;
import com.truemusic.service.FileService;
import com.truemusic.vo.SongVO;
import com.truemusic.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final SongMapper songMapper;
    private final SongFavoriteMapper favoriteMapper;
    private final PlaylistMapper playlistMapper; // 新增注入
    private final FileService fileService;

    /**
     * 1. 获取当前用户信息 (返回带有真实头像URL的 UserVO)
     */
    @GetMapping("/me")
    public Result<?> getMe() {
        Long userId = UserContext.require();
        User user = userMapper.selectById(userId);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        // 把数据库里的路径转换成预签名 URL
        vo.setAvatarUrl(fileService.getPresignedUrl(user.getAvatarUrl()));

        return Result.ok(vo);
    }

    /**
     * 2. 修改个人信息 & 上传头像
     */
    @PostMapping("/profile")
    public Result<?> updateProfile(
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        Long userId = UserContext.require();
        User user = userMapper.selectById(userId);

        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }

        if (avatar != null && !avatar.isEmpty()) {
            // 把头像传到 RustFS 的 hajimi/avatar 目录
            String avatarKey = fileService.uploadFile(avatar, "hajimi/avatar");
            user.setAvatarUrl(avatarKey);
        }

        userMapper.updateById(user);
        return Result.ok("修改成功");
    }

    /**
     * 3. 我的上传
     */
    @GetMapping("/uploads")
    public Result<?> myUploads(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.require();
        Page<Song> p = songMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Song>().eq(Song::getUploaderId, userId).orderByDesc(Song::getCreatedAt));

        Map<String, Object> data = new HashMap<>();
        data.put("records", p.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent());
        data.put("size", p.getSize());
        return Result.ok(data);
    }

    /**
     * 4. 我的收藏 (即“我喜欢的音乐”)
     */
    @GetMapping("/favorites")
    public Result<?> myFavorites(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.require();
        Page<SongFavorite> p = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SongFavorite>().eq(SongFavorite::getUserId, userId).orderByDesc(SongFavorite::getId));

        List<Long> songIds = p.getRecords().stream().map(SongFavorite::getSongId).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        if (!songIds.isEmpty()) {
            List<Song> songs = songMapper.selectBatchIds(songIds);
            data.put("records", songs.stream().map(this::toVO).collect(Collectors.toList()));
        } else {
            data.put("records", List.of());
        }
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent());
        data.put("size", p.getSize());
        return Result.ok(data);
    }

    /**
     * 5. 【新增】获取个人中心聚合数据 (前端一次性拿齐“红心数量”和“所有歌单”)
     */
    @GetMapping("/center")
    public Result<?> userCenter() {
        Long userId = UserContext.require();

        // 1. 查询“我喜欢的音乐”总数量 (红心数量)
        Long favoriteCount = favoriteMapper.selectCount(new LambdaQueryWrapper<SongFavorite>()
                .eq(SongFavorite::getUserId, userId));

        // 2. 查询我创建的所有自定义歌单
        List<Playlist> myPlaylists = playlistMapper.selectList(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getUserId, userId)
                        .orderByDesc(Playlist::getCreatedAt));

        Map<String, Object> data = new HashMap<>();
        data.put("favoriteCount", favoriteCount);
        data.put("playlists", myPlaylists);

        return Result.ok(data);
    }

    /**
     * 辅助方法：转换为 VO 并生成预签名 URL
     */
    private SongVO toVO(Song song) {
        SongVO vo = new SongVO();
        vo.setId(song.getId());
        vo.setTitle(song.getTitle());
        vo.setArtist(song.getArtist());
        vo.setLyric(song.getLyric());
        vo.setLikeCount(song.getLikeCount());
        vo.setAudioUrl(fileService.getPresignedUrl(song.getAudioKey()));
        vo.setCoverUrl(fileService.getPresignedUrl(song.getCoverKey()));
        return vo;
    }
}