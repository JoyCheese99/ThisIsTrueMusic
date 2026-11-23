package com.truemusic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.truemusic.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}