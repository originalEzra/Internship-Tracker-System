package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.reminder.ReminderResponse;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.exception.ReminderNotFoundException;
import com.ezra.internshiptracker.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReminderControllerTest {

    private ReminderService reminderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reminderService = Mockito.mock(ReminderService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReminderController(reminderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyRemindersSupportsStatusFilter() throws Exception {
        when(reminderService.getMyReminders(1L, ReminderStatus.PENDING))
                .thenReturn(List.of(reminder(ReminderStatus.PENDING)));

        mockMvc.perform(get("/api/reminders")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(99))
                .andExpect(jsonPath("$.data[0].internshipId").value(10))
                .andExpect(jsonPath("$.data[0].company").value("OpenAI"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        verify(reminderService).getMyReminders(1L, ReminderStatus.PENDING);
    }

    @Test
    void createReminderUsesCurrentUser() throws Exception {
        when(reminderService.createReminder(Mockito.any(), Mockito.eq(1L)))
                .thenReturn(reminder(ReminderStatus.PENDING));

        mockMvc.perform(post("/api/reminders")
                        .principal(new TestingAuthenticationToken("1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "internshipId": 10,
                                  "message": "OA due tomorrow",
                                  "remindAt": "2027-01-02T10:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        verify(reminderService).createReminder(Mockito.any(), Mockito.eq(1L));
    }

    @Test
    void blankMessageReturns400ApiResponse() throws Exception {
        mockMvc.perform(post("/api/reminders")
                        .principal(new TestingAuthenticationToken("1", null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "internshipId": 10,
                                  "message": " ",
                                  "remindAt": "2027-01-02T10:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("message cannot be blank"));
    }

    @Test
    void invalidStatusParameterReturns400ApiResponse() throws Exception {
        mockMvc.perform(get("/api/reminders")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("status", "DONE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }

    @Test
    void cancelReminderReturnsUpdatedReminder() throws Exception {
        when(reminderService.cancelReminder(99L, 1L))
                .thenReturn(reminder(ReminderStatus.CANCELLED));

        mockMvc.perform(put("/api/reminders/99/cancel")
                        .principal(new TestingAuthenticationToken("1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(99))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void missingReminderReturns404ApiResponse() throws Exception {
        when(reminderService.cancelReminder(99L, 1L))
                .thenThrow(new ReminderNotFoundException("Reminder not found"));

        mockMvc.perform(put("/api/reminders/99/cancel")
                        .principal(new TestingAuthenticationToken("1", null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Reminder not found"));
    }

    private ReminderResponse reminder(ReminderStatus status) {
        ReminderResponse response = new ReminderResponse();
        response.setId(99L);
        response.setInternshipId(10L);
        response.setCompany("OpenAI");
        response.setPosition("Backend Intern");
        response.setMessage("OA due tomorrow");
        response.setRemindAt(LocalDateTime.of(2027, 1, 2, 10, 0));
        response.setStatus(status);
        response.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        response.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return response;
    }
}
