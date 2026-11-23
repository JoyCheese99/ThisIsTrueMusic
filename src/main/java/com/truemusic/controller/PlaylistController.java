package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.truemusic.common.Result;
import com.truemusic.common.UserContext;
import com.truemusic.entity.Playlist;
import com.truemusic.entity.PlaylistSong;
import com.truemusic.entity.Song;
import com.truemusic.mapper.PlaylistMapper;
import com.truemusic.mapper.PlaylistSongMapper;
import com.truemusic.mapper.SongMapper;
import com.truemusic.service.FileService;
import com.truemusic.vo.SongVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistMapper playlistMapper;
    private final PlaylistSongMapper playlistSongMapper;
    private final SongMapper songMapper;
    private final FileService fileService;

    /**
     * 1. 创建歌单 (支持上传封面)
     * 注意：前端请求必须使用 form-data 类型
     */
    @PostMapping
    public Result<?> createPlaylist(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "cover", required = false) MultipartFile cover) {

        Long userId = UserContext.require(); // 必须登录

        if (name == null || name.trim().isEmpty()) {
            return Result.fail(400, "歌单名称不能为空");
        }

        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setName(name.trim());
        playlist.setDescription(description);

        // 如果前端传了封面图，就上传到 RustFS 的 hajimi/playlist_cover 目录
        if (cover != null && !cover.isEmpty()) {
            String coverKey = fileService.uploadFile(cover, "hajimi/playlist_cover");
            playlist.setCoverKey(coverKey);
        }

        playlistMapper.insert(playlist);
        return Result.ok("歌单创建成功");
    }

    /**
     * 2. 获取我的歌单列表
     */
    @GetMapping
    public Result<?> myPlaylists() {
        Long userId = UserContext.require(); // 必须登录

        List<Playlist> playlists = playlistMapper.selectList(
                new LambdaQueryWrapper<Playlist>()
                        .eq(Playlist::getUserId, userId)
                        .orderByDesc(Playlist::getCreatedAt));

        // 将实体转换为带有真实封面 URL 的 Map 列表返回给前端
        List<Map<String, Object>> voList = playlists.stream().map(this::toPlaylistVO).collect(Collectors.toList());

        return Result.ok(voList);
    }

    /**
     * 3. 将歌曲添加到指定歌单
     */
    @PostMapping("/{playlistId}/songs/{songId}")
    public Result<?> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        Long userId = UserContext.require();

        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null || !playlist.getUserId().equals(userId)) {
            return Result.fail(403, "歌单不存在或无权限操作");
        }

        if (songMapper.selectById(songId) == null) {
            return Result.fail(404, "歌曲不存在");
        }

        Long count = playlistSongMapper.selectCount(new LambdaQueryWrapper<PlaylistSong>()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .eq(PlaylistSong::getSongId, songId));
        if (count > 0) {
            return Result.fail(400, "该歌曲已在歌单中");
        }

        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        playlistSongMapper.insert(ps);

        return Result.ok("添加成功");
    }

    /**
     * 4. 获取指定歌单里的歌曲列表
     */
    @GetMapping("/{playlistId}/songs")
    public Result<?> getPlaylistSongs(
            @PathVariable Long playlistId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PlaylistSong> p = playlistSongMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PlaylistSong>()
                        .eq(PlaylistSong::getPlaylistId, playlistId)
                        .orderByDesc(PlaylistSong::getCreatedAt));

        List<Long> songIds = p.getRecords().stream().map(PlaylistSong::getSongId).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        if (!songIds.isEmpty()) {
            List<Song> songs = songMapper.selectBatchIds(songIds);
            List<SongVO> records = songs.stream().map(song -> {
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
            data.put("records", records);
        } else {
            data.put("records", List.of());
        }
        data.put("total", p.getTotal());
        data.put("current", p.getCurrent());
        data.put("size", p.getSize());

        return Result.ok(data);
    }

    /**
     * 辅助方法：将 Playlist 实体转为前端需要的格式（包含带签名的封面 URL）
     */
    private Map<String, Object> toPlaylistVO(Playlist playlist) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", playlist.getId());
        vo.put("name", playlist.getName());
        vo.put("description", playlist.getDescription());
        // 生成预签名封面 URL
        vo.put("coverUrl", fileService.getPresignedUrl(playlist.getCoverKey()));
        vo.put("createdAt", playlist.getCreatedAt());
        return vo;
    }
}