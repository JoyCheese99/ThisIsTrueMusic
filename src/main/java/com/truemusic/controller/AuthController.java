package com.truemusic.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.truemusic.common.Result;
import com.truemusic.dto.LoginDTO;
import com.truemusic.dto.RegisterDTO;
import com.truemusic.entity.User;
import com.truemusic.mapper.UserMapper;
import com.truemusic.utils.JwtUtil;
import com.truemusic.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // 注册接口（临时开放，方便你测试）
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            return Result.fail(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 密码加密
        user.setNickname(dto.getNickname());
        userMapper.insert(user);
        return Result.ok("注册成功");
    }

    // 登录接口
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return Result.fail(400, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId());
        user.setPassword(null); // 不返回密码
        return Result.ok(new LoginVO(token, user));
    }
}