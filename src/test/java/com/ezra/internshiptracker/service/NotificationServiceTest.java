package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.notification.NotificationResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.Notification;
import com.ezra.internshiptracker.entity.NotificationSourceType;
import com.ezra.internshiptracker.entity.NotificationType;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.NotificationNotFoundException;
import com.ezra.internshiptracker.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getMyNotificationsCanReturnUnreadOnly() {
        Notification notification = notification(false);
        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> notifications = notificationService.getMyNotifications(1L, true);

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getId()).isEqualTo(50L);
        assertThat(notifications.get(0).isRead()).isFalse();
        verify(notificationRepository).findByUserIdAndReadFalseOrderByCreatedAtDesc(1L);
    }

    @Test
    void markAsReadRequiresOwnership() {
        Notification notification = notification(false);
        when(notificationRepository.findByIdAndUserId(50L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.markAsRead(50L, 1L);

        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadThrowsWhenNotificationDoesNotBelongToUser() {
        when(notificationRepository.findByIdAndUserId(50L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(50L, 2L))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessage("Notification not found");
    }

    @Test
    void createReminderDueNotificationSavesNotificationForReminder() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 10, 0);
        Reminder reminder = reminder();

        when(notificationRepository.existsBySourceTypeAndSourceId(NotificationSourceType.REMINDER, 99L))
                .thenReturn(false);

        notificationService.createReminderDueNotification(reminder, now);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertThat(notification.getUser()).isSameAs(reminder.getUser());
        assertThat(notification.getType()).isEqualTo(NotificationType.REMINDER_DUE);
        assertThat(notification.getTitle()).isEqualTo("Reminder due");
        assertThat(notification.getContent()).contains("OpenAI", "Backend Intern", "OA due tomorrow");
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getSourceType()).isEqualTo(NotificationSourceType.REMINDER);
        assertThat(notification.getSourceId()).isEqualTo(99L);
        assertThat(notification.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void createReminderDueNotificationSkipsExistingSource() {
        Reminder reminder = reminder();

        when(notificationRepository.existsBySourceTypeAndSourceId(NotificationSourceType.REMINDER, 99L))
                .thenReturn(true);

        notificationService.createReminderDueNotification(reminder, LocalDateTime.now());

        verify(notificationRepository, never()).save(org.mockito.Mockito.any(Notification.class));
    }

    private Notification notification(boolean read) {
        Notification notification = new Notification();
        notification.setId(50L);
        notification.setUser(user());
        notification.setType(NotificationType.REMINDER_DUE);
        notification.setTitle("Reminder due");
        notification.setContent("OpenAI - Backend Intern: OA due tomorrow");
        notification.setRead(read);
        notification.setSourceType(NotificationSourceType.REMINDER);
        notification.setSourceId(99L);
        notification.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        notification.setReadAt(read ? LocalDateTime.of(2026, 1, 2, 11, 0) : null);
        return notification;
    }

    private Reminder reminder() {
        Reminder reminder = new Reminder();
        reminder.setId(99L);
        reminder.setUser(user());
        reminder.setInternship(internship());
        reminder.setMessage("OA due tomorrow");
        return reminder;
    }

    private Internship internship() {
        Internship internship = new Internship();
        internship.setId(10L);
        internship.setCompany("OpenAI");
        internship.setPosition("Backend Intern");
        return internship;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ezra");
        return user;
    }
}

