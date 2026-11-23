package com.truemusic.controller;

import com.truemusic.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.ok("哈基米后端启动成功！");
    }
}