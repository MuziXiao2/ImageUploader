package com.muzixiao2.imageuploader.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 默认 24 小时（毫秒）
    private long expirationTime;

    // 生成 HMAC-SHA 密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 JWT
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    // 提取用户名
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 提取角色
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }

    // 验证令牌
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 解析 Claims
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .clockSkewSeconds(30) // 允许 30 秒时钟偏差
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
