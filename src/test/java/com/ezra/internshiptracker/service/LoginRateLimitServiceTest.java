package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.LoginRateLimitProperties;
import com.ezra.internshiptracker.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginRateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private LoginRateLimitService loginRateLimitService;

    @BeforeEach
    void setUp() {
        LoginRateLimitProperties properties = new LoginRateLimitProperties();
        properties.setMaxFailedAttempts(2);
        properties.setWindowMinutes(15);

        loginRateLimitService = new LoginRateLimitService(redisTemplate, properties);
    }

    @Test
    void checkAllowedThrowsWhenFailureCountReachedLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:login:failures:ezra")).thenReturn("2");

        assertThatThrownBy(() -> loginRateLimitService.checkAllowed("Ezra"))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .hasMessage("Too many failed login attempts. Please try again later.");
    }

    @Test
    void recordFailureSetsTtlOnFirstFailure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("auth:login:failures:ezra")).thenReturn(1L);

        loginRateLimitService.recordFailure("ezra");

        verify(redisTemplate).expire(
                eq("auth:login:failures:ezra"),
                eq(Duration.ofMinutes(15))
        );
    }

    @Test
    void recordFailureThrowsWhenFailureCountReachesLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("auth:login:failures:ezra")).thenReturn(2L);

        assertThatThrownBy(() -> loginRateLimitService.recordFailure("ezra"))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .hasMessage("Too many failed login attempts. Please try again later.");
    }

    @Test
    void clearFailuresDeletesLoginFailureKey() {
        loginRateLimitService.clearFailures("Ezra");

        verify(redisTemplate).delete("auth:login:failures:ezra");
    }
}
