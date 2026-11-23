package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.truemusic.common.Result;
import com.truemusic.common.UserContext;
import com.truemusic.entity.*;
import com.truemusic.mapper.*;
import com.truemusic.service.FileService;
import com.truemusic.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class InteractionController {

    private final SongMapper songMapper;
    private final SongLikeMapper likeMapper;
    private final SongFavoriteMapper favoriteMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final FileService fileService; // 注入文件服务，用于处理头像 URL

    /**
     * 1. 点赞 / 取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<?> toggleLike(@PathVariable Long id) {
        Long userId = UserContext.require();

        // 检查是否已经点过赞
        SongLike existing = likeMapper.selectOne(new LambdaQueryWrapper<SongLike>()
                .eq(SongLike::getUserId, userId).eq(SongLike::getSongId, id));

        boolean isLiked;
        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            songMapper.update(null, new LambdaUpdateWrapper<Song>()
                    .setSql("like_count = like_count - 1").eq(Song::getId, id));
            isLiked = false;
        } else {
            // 点赞
            SongLike like = new SongLike();
            like.setUserId(userId);
            like.setSongId(id);
            likeMapper.insert(like);
            songMapper.update(null, new LambdaUpdateWrapper<Song>()
                    .setSql("like_count = like_count + 1").eq(Song::getId, id));
            isLiked = true;
        }

        Map<String, Boolean> data = new HashMap<>();
        data.put("liked", isLiked);
        return Result.ok(data);
    }

    /**
     * 2. 收藏 / 取消收藏
     */
    @PostMapping("/{id}/favorite")
    public Result<?> toggleFavorite(@PathVariable Long id) {
        Long userId = UserContext.require();

        // 检查是否已经收藏
        SongFavorite existing = favoriteMapper.selectOne(new LambdaQueryWrapper<SongFavorite>()
                .eq(SongFavorite::getUserId, userId).eq(SongFavorite::getSongId, id));

        boolean isFavorited;
        if (existing != null) {
            // 取消收藏
            favoriteMapper.deleteById(existing.getId());
            songMapper.update(null, new LambdaUpdateWrapper<Song>()
                    .setSql("favorite_count = favorite_count - 1").eq(Song::getId, id));
            isFavorited = false;
        } else {
            // 收藏
            SongFavorite fav = new SongFavorite();
            fav.setUserId(userId);
            fav.setSongId(id);
            favoriteMapper.insert(fav);
            songMapper.update(null, new LambdaUpdateWrapper<Song>()
                    .setSql("favorite_count = favorite_count + 1").eq(Song::getId, id));
            isFavorited = true;
        }

        Map<String, Boolean> data = new HashMap<>();
        data.put("favorited", isFavorited);
        return Result.ok(data);
    }

    /**
     * 3. 获取评论列表 (分页)
     */
    @GetMapping("/{id}/comments")
    public Result<?> getComments(@PathVariable Long id,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        // 按时间倒序查询评论
        Page<Comment> p = commentMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getSongId, id)
                        .orderByDesc(Comment::getCreatedAt));

        // 转换为 VO 并补充用户信息
        List<CommentVO> voList = p.getRecords().stream().map(c -> {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setContent(c.getContent());
            vo.setCreatedAt(c.getCreatedAt());

            // 查询发评论的用户信息
            User u = userMapper.selectById(c.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                // 【重点修复】将数据库里的头像 Key 转换为带签名的真实 URL
                vo.setAvatarUrl(fileService.getPresignedUrl(u.getAvatarUrl()));
            } else {
                vo.setNickname("未知用户");
            }
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("records", voList);
        data.put("total", p.getTotal());
        return Result.ok(data);
    }

    /**
     * 4. 发表评论
     */
    @PostMapping("/{id}/comments")
    public Result<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = UserContext.require();
        String content = body.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.fail(400, "评论内容不能为空");
        }
        if (content.length() > 1000) {
            return Result.fail(400, "评论内容过长");
        }

        // 检查歌曲是否存在
        if (songMapper.selectById(id) == null) {
            return Result.fail(404, "歌曲不存在");
        }

        Comment comment = new Comment();
        comment.setSongId(id);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        commentMapper.insert(comment);

        return Result.ok("评论成功");
    }
}