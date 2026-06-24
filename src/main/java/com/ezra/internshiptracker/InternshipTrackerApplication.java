package com.ezra.internshiptracker;

import com.ezra.internshiptracker.config.JwtProperties;
import com.ezra.internshiptracker.config.LoginRateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, LoginRateLimitProperties.class})
public class InternshipTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipTrackerApplication.class, args);
    }

}
