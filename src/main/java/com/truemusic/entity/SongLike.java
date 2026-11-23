// entity/SongLike.java
package com.truemusic.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data @TableName("song_like")
public class SongLike {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long songId;
}