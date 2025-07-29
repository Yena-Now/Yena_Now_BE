package com.example.yenanow.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final StringRedisTemplate redisTemplate;

    private Key key;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostConstruct
    public void initKey() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String userUuid) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .setSubject(userUuid)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + accessTokenExpiration))
            .signWith(key)
            .compact();
    }

    public String generateRefreshToken(String userUuid) {
        long now = System.currentTimeMillis();
        String refreshToken = Jwts.builder()
            .setSubject(userUuid)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + refreshTokenExpiration))
            .signWith(key)
            .compact();

        // Redis에 저장
        redisTemplate.opsForValue().set(
            "refresh_token:" + userUuid,
            refreshToken,
            refreshTokenExpiration,
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