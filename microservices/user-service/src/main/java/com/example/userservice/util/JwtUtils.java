package com.example.userservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    // Đây là Secret Key bí mật (Trong thực tế nên để ở biến môi trường)
    private static final String SECRET = "GameStoreSecretKeyMustBeVeryLongToBeSafe1234567890";
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    /**
     * TẠO TOKEN KHI LOGIN
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
