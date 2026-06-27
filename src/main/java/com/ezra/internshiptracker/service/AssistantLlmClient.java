package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.Reminder;

import java.util.List;

public interface AssistantLlmClient {

    AssistantAdviceResponse generateAdvice(
            Internship internship,
            List<Reminder> pendingReminders,
            AssistantAdviceResponse ruleBasedAdvice
    );
}
