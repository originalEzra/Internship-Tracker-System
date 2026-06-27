package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.config.AssistantLlmProperties;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.ReminderRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AssistantService {

    private final InternshipRepository internshipRepository;
    private final ReminderRepository reminderRepository;
    private final AssistantLlmProperties assistantLlmProperties;
    private final AssistantLlmClient assistantLlmClient;

    public AssistantService(
            InternshipRepository internshipRepository,
            ReminderRepository reminderRepository,
            AssistantLlmProperties assistantLlmProperties,
            AssistantLlmClient assistantLlmClient
    ) {
        this.internshipRepository = internshipRepository;
        this.reminderRepository = reminderRepository;
        this.assistantLlmProperties = assistantLlmProperties;
        this.assistantLlmClient = assistantLlmClient;
    }

    public AssistantAdviceResponse getAdvice(Long internshipId, Long userId) {
        Internship internship = internshipRepository.findByIdAndUserId(internshipId, userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        List<Reminder> pendingReminders =
                reminderRepository.findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
                        internshipId,
                        userId,
                        ReminderStatus.PENDING
                );

        AssistantAdviceResponse ruleBasedAdvice = buildRuleBasedAdvice(internship, pendingReminders);

        if (!shouldUseLlm()) {
            return ruleBasedAdvice;
        }

        try {
            return assistantLlmClient.generateAdvice(internship, pendingReminders, ruleBasedAdvice);
        } catch (RuntimeException e) {
            return ruleBasedAdvice;
        }
    }

    private AssistantAdviceResponse buildRuleBasedAdvice(Internship internship, List<Reminder> pendingReminders) {
        AssistantAdviceResponse response = new AssistantAdviceResponse();
        response.setInternshipId(internship.getId());
        response.setStatus(internship.getStatus());
        response.setSummary(buildSummary(internship));
        response.setSuggestions(buildSuggestions(internship, pendingReminders));
        return response;
    }

    private boolean shouldUseLlm() {
        return assistantLlmProperties.isEnabled()
                && assistantLlmProperties.hasApiKey()
                && "openai".equalsIgnoreCase(assistantLlmProperties.getProvider());
    }

    private String buildSummary(Internship internship) {
        return switch (internship.getStatus()) {
            case DRAFT -> "This application is still a draft.";
            case APPLIED -> "You have applied and are waiting for progress.";
            case ONLINE_ASSESSMENT -> "You are currently in the online assessment stage.";
            case TECH_INTERVIEW -> "You are currently in the technical interview stage.";
            case HR_INTERVIEW -> "You are currently in the HR interview stage.";
            case OFFER -> "You have reached the offer stage.";
            case REJECTED -> "This application has been rejected.";
            case WITHDRAWN -> "You have withdrawn this application.";
        };
    }

    private List<String> buildSuggestions(Internship internship, List<Reminder> pendingReminders) {
        List<String> suggestions = new ArrayList<>();
        InternshipStatus status = internship.getStatus();

        if (status == InternshipStatus.APPLIED && daysSinceUpdate(internship) >= 7) {
            suggestions.add("You applied more than 7 days ago without a status update. Consider sending a polite follow-up email.");
        }

        switch (status) {
            case DRAFT -> suggestions.add("Review the job description, resume fit, and application materials before submitting.");
            case APPLIED -> suggestions.add("Keep tracking the application and add a follow-up reminder if you do not hear back soon.");
            case ONLINE_ASSESSMENT -> suggestions.add("Prioritize the online assessment and review core data structures, algorithms, and time management.");
            case TECH_INTERVIEW -> suggestions.add("Prepare a 2-minute project introduction and review Java, Spring Boot, JWT, Redis, MySQL, and transaction questions.");
            case HR_INTERVIEW -> suggestions.add("Prepare behavioral stories using the STAR method and clarify motivation, teamwork, and conflict examples.");
            case OFFER -> suggestions.add("Confirm the offer deadline, compensation, location, start date, visa requirements, and competing options.");
            case REJECTED -> suggestions.add("Record feedback if available and reuse the experience to improve future applications.");
            case WITHDRAWN -> suggestions.add("Keep a short note explaining why you withdrew so future decisions are easier to compare.");
        }

        if (hasUpcomingReminder(pendingReminders)) {
            suggestions.add("You have a pending reminder for this application. Check the deadline and handle it before it becomes urgent.");
        } else if (status == InternshipStatus.ONLINE_ASSESSMENT
                || status == InternshipStatus.TECH_INTERVIEW
                || status == InternshipStatus.HR_INTERVIEW
                || status == InternshipStatus.OFFER) {
            suggestions.add("No pending reminder was found for this stage. Add one for the assessment, interview, or offer deadline.");
        }

        return suggestions;
    }

    private long daysSinceUpdate(Internship internship) {
        LocalDateTime updatedAt = internship.getUpdatedAt();
        if (updatedAt == null) {
            updatedAt = internship.getCreatedAt();
        }

        return Duration.between(updatedAt, LocalDateTime.now()).toDays();
    }

    private boolean hasUpcomingReminder(List<Reminder> pendingReminders) {
        return pendingReminders.stream()
                .anyMatch(reminder -> !reminder.getRemindAt().isBefore(LocalDateTime.now()));
    }
}
