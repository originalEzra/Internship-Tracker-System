package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.LoginRateLimitProperties;
import com.ezra.internshiptracker.exception.TooManyLoginAttemptsException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Locale;

@Service
public class LoginRateLimitService {

    private static final String LOGIN_FAILURE_PREFIX = "auth:login:failures:";
    private static final String TOO_MANY_ATTEMPTS_MESSAGE =
            "Too many failed login attempts. Please try again later.";

    private final StringRedisTemplate redisTemplate;
    private final LoginRateLimitProperties properties;

    public LoginRateLimitService(StringRedisTemplate redisTemplate,
                                 LoginRateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public void checkAllowed(String username) {
        String value = redisTemplate.opsForValue().get(key(username));
        int failures = value == null ? 0 : Integer.parseInt(value);

        if (failures >= properties.getMaxFailedAttempts()) {
            throw new TooManyLoginAttemptsException(TOO_MANY_ATTEMPTS_MESSAGE);
        }
    }

    public void recordFailure(String username) {
        Long failures = redisTemplate.opsForValue().increment(key(username));

        if (failures != null && failures == 1L) {
            redisTemplate.expire(key(username), Duration.ofMinutes(properties.getWindowMinutes()));
        }

        if (failures != null && failures >= properties.getMaxFailedAttempts()) {
            throw new TooManyLoginAttemptsException(TOO_MANY_ATTEMPTS_MESSAGE);
        }
    }

    public void clearFailures(String username) {
        redisTemplate.delete(key(username));
    }

    private String key(String username) {
        String normalizedUsername = username == null
                ? "unknown"
                : username.trim().toLowerCase(Locale.ROOT);

        return LOGIN_FAILURE_PREFIX + normalizedUsername;
    }
}
