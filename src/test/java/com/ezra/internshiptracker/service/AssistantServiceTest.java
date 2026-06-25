package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.ReminderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTest {

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private ReminderRepository reminderRepository;

    @InjectMocks
    private AssistantService assistantService;

    @Test
    void appliedForMoreThanSevenDaysSuggestsFollowUp() {
        Internship internship = internship(InternshipStatus.APPLIED);
        internship.setUpdatedAt(LocalDateTime.now().minusDays(8));

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of());

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getInternshipId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(InternshipStatus.APPLIED);
        assertThat(response.getSummary()).contains("waiting for progress");
        assertThat(response.getSuggestions())
                .anyMatch(suggestion -> suggestion.contains("follow-up"));
    }

    @Test
    void onlineAssessmentWithPendingReminderMentionsReminder() {
        Internship internship = internship(InternshipStatus.ONLINE_ASSESSMENT);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of(reminder(LocalDateTime.now().plusDays(1))));

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getSummary()).contains("online assessment");
        assertThat(response.getSuggestions())
                .anyMatch(suggestion -> suggestion.contains("online assessment"))
                .anyMatch(suggestion -> suggestion.contains("pending reminder"));
    }

    @Test
    void interviewStageWithoutReminderSuggestsAddingReminder() {
        Internship internship = internship(InternshipStatus.TECH_INTERVIEW);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of());

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getSuggestions())
                .anyMatch(suggestion -> suggestion.contains("Java"))
                .anyMatch(suggestion -> suggestion.contains("No pending reminder"));
    }

    @Test
    void offerStageSuggestsOfferDecisionChecklist() {
        Internship internship = internship(InternshipStatus.OFFER);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of());

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getSuggestions())
                .anyMatch(suggestion -> suggestion.contains("offer deadline"))
                .anyMatch(suggestion -> suggestion.contains("compensation"));
    }

    @Test
    void getAdviceRequiresInternshipOwnership() {
        when(internshipRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assistantService.getAdvice(10L, 2L))
                .isInstanceOf(InternshipNotFoundException.class)
                .hasMessage("Internship not found");
    }

    private Internship internship(InternshipStatus status) {
        Internship internship = new Internship();
        internship.setId(10L);
        internship.setCompany("OpenAI");
        internship.setPosition("Backend Intern");
        internship.setLocation("Sydney");
        internship.setStatus(status);
        internship.setApplicationUrl("https://example.com");
        internship.setCreatedAt(LocalDateTime.now().minusDays(10));
        internship.setUpdatedAt(LocalDateTime.now().minusDays(1));
        internship.setUser(user());
        return internship;
    }

    private Reminder reminder(LocalDateTime remindAt) {
        Reminder reminder = new Reminder();
        reminder.setId(99L);
        reminder.setUser(user());
        reminder.setInternship(internship(InternshipStatus.ONLINE_ASSESSMENT));
        reminder.setMessage("OA due tomorrow");
        reminder.setRemindAt(remindAt);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setCreatedAt(LocalDateTime.now());
        reminder.setUpdatedAt(LocalDateTime.now());
        return reminder;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ezra");
        return user;
    }
}
