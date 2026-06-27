package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.AssistantLlmProperties;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceSource;
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
import org.mockito.Mockito;
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

    @Mock
    private AssistantLlmProperties assistantLlmProperties;

    @Mock
    private AssistantLlmClient assistantLlmClient;

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
        assertThat(response.getSource()).isEqualTo(AssistantAdviceSource.RULE_BASED);
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
    void llmAdviceIsUsedWhenEnabledAndConfigured() {
        Internship internship = internship(InternshipStatus.TECH_INTERVIEW);
        AssistantAdviceResponse llmAdvice = new AssistantAdviceResponse();
        llmAdvice.setInternshipId(10L);
        llmAdvice.setStatus(InternshipStatus.TECH_INTERVIEW);
        llmAdvice.setSummary("LLM enhanced interview summary.");
        llmAdvice.setSuggestions(List.of("Use the LLM enhanced checklist."));
        llmAdvice.setSource(AssistantAdviceSource.LLM);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of());
        when(assistantLlmProperties.isEnabled()).thenReturn(true);
        when(assistantLlmProperties.hasApiKey()).thenReturn(true);
        when(assistantLlmProperties.getProvider()).thenReturn("openai");
        when(assistantLlmClient.generateAdvice(Mockito.eq(internship), Mockito.eq(List.of()), Mockito.any()))
                .thenReturn(llmAdvice);

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getSource()).isEqualTo(AssistantAdviceSource.LLM);
        assertThat(response.getSummary()).isEqualTo("LLM enhanced interview summary.");
        assertThat(response.getSuggestions()).containsExactly("Use the LLM enhanced checklist.");
    }

    @Test
    void llmFailureFallsBackToRuleBasedAdvice() {
        Internship internship = internship(InternshipStatus.TECH_INTERVIEW);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                10L,
                1L,
                ReminderStatus.PENDING
        )).thenReturn(List.of());
        when(assistantLlmProperties.isEnabled()).thenReturn(true);
        when(assistantLlmProperties.hasApiKey()).thenReturn(true);
        when(assistantLlmProperties.getProvider()).thenReturn("openai");
        when(assistantLlmClient.generateAdvice(Mockito.eq(internship), Mockito.eq(List.of()), Mockito.any()))
                .thenThrow(new IllegalStateException("LLM unavailable"));

        AssistantAdviceResponse response = assistantService.getAdvice(10L, 1L);

        assertThat(response.getSource()).isEqualTo(AssistantAdviceSource.RULE_BASED);
        assertThat(response.getSummary()).contains("technical interview");
        assertThat(response.getSuggestions())
                .anyMatch(suggestion -> suggestion.contains("Java"));
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
