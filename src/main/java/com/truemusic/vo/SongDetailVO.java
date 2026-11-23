package com.truemusic.vo;
import lombok.Data;
@Data
public class SongDetailVO extends SongVO {
    private Boolean liked;      // 当前用户是否点赞
    private Boolean favorited;  // 当前用户是否收藏
}