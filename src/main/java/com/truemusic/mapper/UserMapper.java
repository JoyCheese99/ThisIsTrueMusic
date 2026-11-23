package com.truemusic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.truemusic.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}