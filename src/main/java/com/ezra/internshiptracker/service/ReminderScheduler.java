package com.ezra.internshiptracker.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "reminder.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduler {

    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(fixedDelayString = "${reminder.scheduler.fixed-delay-ms:60000}")
    public void processDueReminders() {
        reminderService.markDueRemindersAsSent(LocalDateTime.now());
    }
}
