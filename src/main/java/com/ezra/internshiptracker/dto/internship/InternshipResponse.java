package com.ezra.internshiptracker.dto.internship;

import com.ezra.internshiptracker.entity.InternshipStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InternshipResponse {

    private Long id;

    private String company;

    private String position;

    private String location;

    private InternshipStatus status;

    private String applicationUrl;

    private LocalDateTime createdAt;
}
