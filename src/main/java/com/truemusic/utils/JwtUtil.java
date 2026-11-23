package com.truemusic.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire-hours}")
    private long expireHours;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 Token
    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireHours * 3600_000))
                .signWith(getKey())
                .compact();
    }

    // 解析 Token
    public Long parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}