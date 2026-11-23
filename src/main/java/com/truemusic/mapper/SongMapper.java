package com.truemusic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.truemusic.entity.Song;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongMapper extends BaseMapper<Song> { // 必须是 Song
}