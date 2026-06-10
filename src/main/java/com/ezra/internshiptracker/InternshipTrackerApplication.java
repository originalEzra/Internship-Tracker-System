package com.ezra.internshiptracker;

import com.ezra.internshiptracker.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class InternshipTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipTrackerApplication.class, args);
    }

}
