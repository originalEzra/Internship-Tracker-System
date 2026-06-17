package com.ezra.internshiptracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "jwt.secret=test-secret-key-with-at-least-32-bytes",
                    "jwt.expiration-ms=60000",
                    "jwt.refresh-expiration-ms=604800000"
            );

    @Test
    void bindsJwtPropertiesFromConfiguration() {
        contextRunner.run(context -> {
            JwtProperties jwtProperties = context.getBean(JwtProperties.class);

            assertThat(jwtProperties.getSecret())
                    .isEqualTo("test-secret-key-with-at-least-32-bytes");
            assertThat(jwtProperties.getExpirationMs()).isEqualTo(60000L);
            assertThat(jwtProperties.getRefreshExpirationMs()).isEqualTo(604800000L);
        });
    }

    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {
    }
}
