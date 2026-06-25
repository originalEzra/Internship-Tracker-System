package com.ezra.internshiptracker.dto.reminder;

import com.ezra.internshiptracker.entity.ReminderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReminderResponse {

    private Long id;

    private Long internshipId;

    private String company;

    private String position;

    private String message;

    private LocalDateTime remindAt;

    private ReminderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
