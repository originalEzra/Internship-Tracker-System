package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.notification.NotificationResponse;
import com.ezra.internshiptracker.entity.Notification;
import com.ezra.internshiptracker.entity.NotificationSourceType;
import com.ezra.internshiptracker.entity.NotificationType;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.exception.NotificationNotFoundException;
import com.ezra.internshiptracker.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getMyNotifications(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        return toResponse(notificationRepository.save(notification));
    }

    public void createReminderDueNotification(Reminder reminder, LocalDateTime now) {
        if (notificationRepository.existsBySourceTypeAndSourceId(NotificationSourceType.REMINDER, reminder.getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(reminder.getUser());
        notification.setType(NotificationType.REMINDER_DUE);
        notification.setTitle("Reminder due");
        notification.setContent(buildReminderContent(reminder));
        notification.setRead(false);
        notification.setSourceType(NotificationSourceType.REMINDER);
        notification.setSourceId(reminder.getId());
        notification.setCreatedAt(now);

        notificationRepository.save(notification);
    }

    private String buildReminderContent(Reminder reminder) {
        return "%s - %s: %s".formatted(
                reminder.getInternship().getCompany(),
                reminder.getInternship().getPosition(),
                reminder.getMessage()
        );
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setRead(notification.isRead());
        response.setSourceType(notification.getSourceType());
        response.setSourceId(notification.getSourceId());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        return response;
    }
}

