package com.truemusic.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CommentVO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String nickname;  // 评论人昵称
    private String avatarUrl; // 评论人头像
}