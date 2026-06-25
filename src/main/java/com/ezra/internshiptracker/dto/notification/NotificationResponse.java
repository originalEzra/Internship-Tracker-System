package com.ezra.internshiptracker.dto.notification;

import com.ezra.internshiptracker.entity.NotificationSourceType;
import com.ezra.internshiptracker.entity.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {

    private Long id;

    private NotificationType type;

    private String title;

    private String content;

    private boolean read;

    private NotificationSourceType sourceType;

    private Long sourceId;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}

