package com.truemusic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.truemusic.entity.SongFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongFavoriteMapper extends BaseMapper<SongFavorite> {
}