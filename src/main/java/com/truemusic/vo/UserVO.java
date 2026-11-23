package com.truemusic.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl; // 这里是带签名的真实链接
}