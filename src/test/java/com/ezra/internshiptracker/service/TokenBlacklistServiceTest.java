package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate, jwtUtil);
    }

    @Test
    void blacklistAccessTokenStoresHashedTokenWithRemainingTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtUtil.validateToken("access-token")).thenReturn(true);
        when(jwtUtil.remainingTtl("access-token")).thenReturn(Duration.ofMinutes(10));

        tokenBlacklistService.blacklistAccessToken("access-token");

        verify(valueOperations).set(
                startsWith("auth:blacklist:access:"),
                eq("blacklisted"),
                eq(Duration.ofMinutes(10))
        );
    }

    @Test
    void invalidAccessTokenIsNotStored() {
        when(jwtUtil.validateToken("bad-token")).thenReturn(false);

        tokenBlacklistService.blacklistAccessToken("bad-token");

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void isAccessTokenBlacklistedChecksRedisKey() {
        when(redisTemplate.hasKey(startsWith("auth:blacklist:access:"))).thenReturn(true);

        boolean blacklisted = tokenBlacklistService.isAccessTokenBlacklisted("access-token");

        assertThat(blacklisted).isTrue();
    }
}
