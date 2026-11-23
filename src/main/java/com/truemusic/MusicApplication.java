package com.truemusic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.truemusic.mapper") // 必须加，否则找不到数据库操作
public class MusicApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicApplication.class, args);
    }
}