package com.example.yenanow.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final StringRedisTemplate redisTemplate;

    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    private final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60;         // 1시간
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7일

    public String generateToken(String userUuid) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(userUuid)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String userUuid) {
        long now = System.currentTimeMillis();
        String refreshToken = Jwts.builder()
                .setSubject(userUuid)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();

        // Redis에 저장
        redisTemplate.opsForValue().set(
                "refresh_token:" + userUuid,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION,
                TimeUnit.MILLISECONDS
        );

        return refreshToken;
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}