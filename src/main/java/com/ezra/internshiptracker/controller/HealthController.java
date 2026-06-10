package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Internship Tracker Running!");
    }
}
