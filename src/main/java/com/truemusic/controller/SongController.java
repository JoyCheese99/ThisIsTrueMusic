package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.truemusic.common.Result;
import com.truemusic.common.UserContext;
import com.truemusic.entity.Song;
import com.truemusic.entity.SongFavorite;
import com.truemusic.entity.SongLike;
import com.truemusic.mapper.SongFavoriteMapper;
import com.truemusic.mapper.SongLikeMapper;
import com.truemusic.mapper.SongMapper;
import com.truemusic.service.FileService;
import com.truemusic.vo.SongDetailVO;
import com.truemusic.vo.SongVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongMapper songMapper;
    private final FileService fileService;
    private final SongLikeMapper songLikeMapper;
    private final SongFavoriteMapper songFavoriteMapper;

    /**
     * 1. 获取歌曲列表 (支持分页、关键词搜索、分类筛选)
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        // 构造查询条件
        LambdaQueryWrapper<Song> wrapper = new LambdaQueryWrapper<>();

        // 分类筛选：如果传了 categoryId，就加上 eq 条件
        wrapper.eq(categoryId != null, Song::getCategoryId, categoryId);

        // 关键词搜索：如果 keyword 不为空，搜索歌曲名或歌手名
        // 注意这里的 .and(w -> ...)，是为了保证生成的 SQL 是 AND (title LIKE ? OR artist LIKE ?)
        // 如果不加括号，可能会和前面的 categoryId 产生逻辑覆盖
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Song::getTitle, keyword)
                    .or()
                    .like(Song::getArtist, keyword));
        }

        // 排序
        wrapper.orderByDesc(Song::getCreatedAt);

        // 执行分页查询
        Page<Song> p = songMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换结果
        List<SongVO> records = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        // 完全按照文档要求封装返回格式
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent()); // 当前页码
        data.put("size", p.getSize());       // 每页条数

        return Result.ok(data);
    }

    /**
     * 2. 上传歌曲 (必须登录)
     */
    @PostMapping
    public Result<?> upload(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam(required = false) String lyric,
            @RequestParam Long categoryId) {

        Long userId = UserContext.require();

        String audioKey = fileService.uploadFile(audio, "hajimi/mp3");
        String coverKey = cover != null ? fileService.uploadFile(cover, "hajimi/cover") : null;

        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        song.setAudioKey(audioKey);
        song.setCoverKey(coverKey);
        song.setLyric(lyric);
        song.setCategoryId(categoryId);
        song.setUploaderId(userId);
        song.setLikeCount(0);
        song.setFavoriteCount(0);

        songMapper.insert(song);
        return Result.ok("上传成功");
    }

    /**
     * 3. 获取歌曲详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Song song = songMapper.selectById(id);
        if (song == null) return Result.fail(404, "歌曲不存在");

        SongDetailVO vo = new SongDetailVO();
        vo.setId(song.getId());
        vo.setTitle(song.getTitle());
        vo.setArtist(song.getArtist());
        vo.setLyric(song.getLyric());
        vo.setLikeCount(song.getLikeCount());
        vo.setAudioUrl(fileService.getPresignedUrl(song.getAudioKey()));
        vo.setCoverUrl(fileService.getPresignedUrl(song.getCoverKey()));

        Long userId = UserContext.get();
        if (userId != null) {
            boolean isLiked = songLikeMapper.selectCount(new LambdaQueryWrapper<SongLike>()
                    .eq(SongLike::getUserId, userId).eq(SongLike::getSongId, id)) > 0;
            boolean isFavorited = songFavoriteMapper.selectCount(new LambdaQueryWrapper<SongFavorite>()
                    .eq(SongFavorite::getUserId, userId).eq(SongFavorite::getSongId, id)) > 0;
            vo.setLiked(isLiked);
            vo.setFavorited(isFavorited);
        } else {
            vo.setLiked(false);
            vo.setFavorited(false);
        }

        return Result.ok(vo);
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