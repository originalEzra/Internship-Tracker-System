package com.ezra.internshiptracker.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    @Test
    void generatedTokenCanBeValidatedAndParsedBackToUserId() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-key-with-at-least-32-bytes", 3_600_000L);

        String token = jwtUtil.generateToken(123L);

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(123L);
    }

    @Test
    void invalidTokenIsRejected() {
        JwtUtil jwtUtil = new JwtUtil("test-secret-key-with-at-least-32-bytes", 3_600_000L);

        assertThat(jwtUtil.validateToken("not-a-valid-token")).isFalse();
    }
}
