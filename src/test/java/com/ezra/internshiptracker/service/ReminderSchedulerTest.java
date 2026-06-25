package com.ezra.internshiptracker.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReminderSchedulerTest {

    @Test
    void processDueRemindersDelegatesToReminderService() {
        ReminderService reminderService = mock(ReminderService.class);
        ReminderScheduler scheduler = new ReminderScheduler(reminderService);

        scheduler.processDueReminders();

        verify(reminderService).markDueRemindersAsSent(any(LocalDateTime.class));
    }
}
