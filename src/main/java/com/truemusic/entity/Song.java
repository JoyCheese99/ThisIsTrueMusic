package com.truemusic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Song {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String artist;
    private String audioKey;
    private String coverKey;
    private String lyric;
    private Long categoryId;
    private Long uploaderId;
    private Integer likeCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
}