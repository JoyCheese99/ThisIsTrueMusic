

// entity/SongFavorite.java
package com.truemusic.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data @TableName("song_favorite")
public class SongFavorite {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Long songId;
}
