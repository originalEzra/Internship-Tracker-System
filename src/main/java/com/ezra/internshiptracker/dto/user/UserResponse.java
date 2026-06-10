package com.ezra.internshiptracker.dto.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}