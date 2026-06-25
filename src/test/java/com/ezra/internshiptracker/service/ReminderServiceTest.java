package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.reminder.CreateReminderRequest;
import com.ezra.internshiptracker.dto.reminder.ReminderResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.exception.ReminderNotFoundException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.ReminderRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReminderService reminderService;

    @Test
    void createReminderRequiresInternshipOwnership() {
        Internship internship = internship();

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(invocation -> {
            Reminder reminder = invocation.getArgument(0);
            reminder.setId(99L);
            return reminder;
        });

        ReminderResponse response = reminderService.createReminder(createRequest(), 1L);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getInternshipId()).isEqualTo(10L);
        assertThat(response.getCompany()).isEqualTo("OpenAI");
        assertThat(response.getPosition()).isEqualTo("Backend Intern");
        assertThat(response.getMessage()).isEqualTo("OA due tomorrow");
        assertThat(response.getStatus()).isEqualTo(ReminderStatus.PENDING);
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        ArgumentCaptor<Reminder> reminderCaptor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderRepository).save(reminderCaptor.capture());

        Reminder reminder = reminderCaptor.getValue();
        assertThat(reminder.getUser()).isSameAs(internship.getUser());
        assertThat(reminder.getInternship()).isSameAs(internship);
        assertThat(reminder.getMessage()).isEqualTo("OA due tomorrow");
        assertThat(reminder.getStatus()).isEqualTo(ReminderStatus.PENDING);
    }

    @Test
    void createReminderRejectsInternshipThatDoesNotBelongToCurrentUser() {
        when(internshipRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.createReminder(createRequest(), 2L))
                .isInstanceOf(InternshipNotFoundException.class)
                .hasMessage("Internship not found");
    }

    @Test
    void getMyRemindersCanFilterByStatus() {
        Reminder reminder = reminder(ReminderStatus.PENDING);

        when(reminderRepository.findByUserIdAndStatusOrderByRemindAtAsc(1L, ReminderStatus.PENDING))
                .thenReturn(List.of(reminder));

        List<ReminderResponse> reminders = reminderService.getMyReminders(1L, ReminderStatus.PENDING);

        assertThat(reminders).hasSize(1);
        assertThat(reminders.get(0).getId()).isEqualTo(99L);
        assertThat(reminders.get(0).getStatus()).isEqualTo(ReminderStatus.PENDING);
        verify(reminderRepository).findByUserIdAndStatusOrderByRemindAtAsc(1L, ReminderStatus.PENDING);
    }

    @Test
    void getMyRemindersReturnsAllWhenStatusIsNull() {
        Reminder reminder = reminder(ReminderStatus.PENDING);

        when(reminderRepository.findByUserIdOrderByRemindAtAsc(1L)).thenReturn(List.of(reminder));

        List<ReminderResponse> reminders = reminderService.getMyReminders(1L, null);

        assertThat(reminders).hasSize(1);
        verify(reminderRepository).findByUserIdOrderByRemindAtAsc(1L);
    }

    @Test
    void cancelReminderRequiresOwnership() {
        Reminder reminder = reminder(ReminderStatus.PENDING);

        when(reminderRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.of(reminder));
        when(reminderRepository.save(reminder)).thenReturn(reminder);

        ReminderResponse response = reminderService.cancelReminder(99L, 1L);

        assertThat(response.getStatus()).isEqualTo(ReminderStatus.CANCELLED);
        assertThat(response.getUpdatedAt()).isAfter(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    @Test
    void cancelReminderThrowsWhenReminderDoesNotBelongToCurrentUser() {
        when(reminderRepository.findByIdAndUserId(99L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reminderService.cancelReminder(99L, 2L))
                .isInstanceOf(ReminderNotFoundException.class)
                .hasMessage("Reminder not found");
    }

    @Test
    void markDueRemindersAsSentUpdatesPendingDueReminders() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 3, 10, 0);
        Reminder dueReminder = reminder(ReminderStatus.PENDING);
        dueReminder.setRemindAt(now.minusMinutes(5));

        when(reminderRepository.findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
                ReminderStatus.PENDING,
                now
        )).thenReturn(List.of(dueReminder));

        int processed = reminderService.markDueRemindersAsSent(now);

        assertThat(processed).isEqualTo(1);
        assertThat(dueReminder.getStatus()).isEqualTo(ReminderStatus.SENT);
        assertThat(dueReminder.getUpdatedAt()).isEqualTo(now);
        verify(notificationService).createReminderDueNotification(dueReminder, now);
        verify(reminderRepository).saveAll(List.of(dueReminder));
    }

    private CreateReminderRequest createRequest() {
        CreateReminderRequest request = new CreateReminderRequest();
        request.setInternshipId(10L);
        request.setMessage("  OA due tomorrow  ");
        request.setRemindAt(LocalDateTime.now().plusDays(1));
        return request;
    }

    private Reminder reminder(ReminderStatus status) {
        Reminder reminder = new Reminder();
        reminder.setId(99L);
        reminder.setUser(user());
        reminder.setInternship(internship());
        reminder.setMessage("OA due tomorrow");
        reminder.setRemindAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        reminder.setStatus(status);
        reminder.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        reminder.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return reminder;
    }

    private Internship internship() {
        Internship internship = new Internship();
        internship.setId(10L);
        internship.setCompany("OpenAI");
        internship.setPosition("Backend Intern");
        internship.setLocation("Sydney");
        internship.setStatus(InternshipStatus.ONLINE_ASSESSMENT);
        internship.setApplicationUrl("https://example.com");
        internship.setUser(user());
        return internship;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ezra");
        return user;
    }
}
