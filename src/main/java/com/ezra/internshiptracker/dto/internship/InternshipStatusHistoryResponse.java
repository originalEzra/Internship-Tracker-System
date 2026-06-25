package com.ezra.internshiptracker.dto.internship;

import com.ezra.internshiptracker.entity.InternshipStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InternshipStatusHistoryResponse {

    private Long id;

    private Long internshipId;

    private InternshipStatus fromStatus;

    private InternshipStatus toStatus;

    private Long operatorUserId;

    private String operatorUsername;

    private String note;

    private LocalDateTime createdAt;
}
