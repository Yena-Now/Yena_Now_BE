package com.example.yenanow.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static String generateToken(String userUuid) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .setSubject(userUuid)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 1000 * 60 * 60))
            .signWith(key)
            .compact();
    }
}
