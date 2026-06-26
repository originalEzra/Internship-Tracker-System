package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.notification.NotificationResponse;
import com.ezra.internshiptracker.entity.NotificationSourceType;
import com.ezra.internshiptracker.entity.NotificationType;
import com.ezra.internshiptracker.exception.GlobalExceptionHandler;
import com.ezra.internshiptracker.exception.NotificationNotFoundException;
import com.ezra.internshiptracker.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        notificationService = Mockito.mock(NotificationService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(notificationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyNotificationsSupportsUnreadOnly() throws Exception {
        when(notificationService.getMyNotifications(1L, true))
                .thenReturn(List.of(notification(false)));

        mockMvc.perform(get("/api/notifications")
                        .principal(new TestingAuthenticationToken("1", null))
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(50))
                .andExpect(jsonPath("$.data[0].type").value("REMINDER_DUE"))
                .andExpect(jsonPath("$.data[0].read").value(false))
                .andExpect(jsonPath("$.data[0].sourceType").value("REMINDER"))
                .andExpect(jsonPath("$.data[0].sourceId").value(99));

        verify(notificationService).getMyNotifications(1L, true);
    }

    @Test
    void markAsReadUsesCurrentUser() throws Exception {
        when(notificationService.markAsRead(50L, 1L)).thenReturn(notification(true));

        mockMvc.perform(put("/api/notifications/50/read")
                        .principal(new TestingAuthenticationToken("1", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(50))
                .andExpect(jsonPath("$.data.read").value(true))
                .andExpect(jsonPath("$.data.readAt").isNotEmpty());

        verify(notificationService).markAsRead(50L, 1L);
    }

    @Test
    void missingNotificationReturns404ApiResponse() throws Exception {
        when(notificationService.markAsRead(50L, 2L))
                .thenThrow(new NotificationNotFoundException("Notification not found"));

        mockMvc.perform(put("/api/notifications/50/read")
                        .principal(new TestingAuthenticationToken("2", null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    private NotificationResponse notification(boolean read) {
        NotificationResponse response = new NotificationResponse();
        response.setId(50L);
        response.setType(NotificationType.REMINDER_DUE);
        response.setTitle("Reminder due");
        response.setContent("OpenAI - Backend Intern: OA due tomorrow");
        response.setRead(read);
        response.setSourceType(NotificationSourceType.REMINDER);
        response.setSourceId(99L);
        response.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        response.setReadAt(read ? LocalDateTime.of(2026, 1, 2, 11, 0) : null);
        return response;
    }
}

