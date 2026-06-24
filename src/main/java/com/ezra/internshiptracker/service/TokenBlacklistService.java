package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:access:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtUtil jwtUtil) {
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || !jwtUtil.validateToken(accessToken)) {
            return;
        }

        Duration ttl = jwtUtil.remainingTtl(accessToken);
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }

        redisTemplate.opsForValue().set(key(accessToken), "blacklisted", ttl);
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(key(accessToken)));
    }

    private String key(String token) {
        return BLACKLIST_PREFIX + sha256(token);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
