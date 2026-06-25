package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.ApiResponse;
import com.ezra.internshiptracker.dto.reminder.CreateReminderRequest;
import com.ezra.internshiptracker.dto.reminder.ReminderResponse;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.service.ReminderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    @GetMapping
    public ApiResponse<List<ReminderResponse>> getMyReminders(
            Authentication authentication,
            @RequestParam(required = false) ReminderStatus status
    ) {
        Long userId = getCurrentUserId(authentication);

        return ApiResponse.success(reminderService.getMyReminders(userId, status));
    }

    @PostMapping
    public ApiResponse<ReminderResponse> createReminder(
            @Valid @RequestBody CreateReminderRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        return ApiResponse.success(reminderService.createReminder(request, userId));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<ReminderResponse> cancelReminder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        return ApiResponse.success(reminderService.cancelReminder(id, userId));
    }
}
