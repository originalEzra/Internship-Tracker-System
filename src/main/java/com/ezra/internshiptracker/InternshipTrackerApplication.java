package com.ezra.internshiptracker;

import com.ezra.internshiptracker.config.AssistantLlmProperties;
import com.ezra.internshiptracker.config.JwtProperties;
import com.ezra.internshiptracker.config.LoginRateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
        JwtProperties.class,
        LoginRateLimitProperties.class,
        AssistantLlmProperties.class
})
public class InternshipTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipTrackerApplication.class, args);
    }

}
