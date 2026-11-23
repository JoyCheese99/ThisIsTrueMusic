// vo/SongVO.java
package com.truemusic.vo;
import lombok.Data;

@Data
public class SongVO {
    private Long id;
    private String title;
    private String artist;
    private String audioUrl;  // 预签名链接
    private String coverUrl;  // 预签名链接
    private String lyric;
    private Integer likeCount;
}