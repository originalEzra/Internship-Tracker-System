package com.ezra.internshiptracker.dto.reminder;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateReminderRequest {

    @NotNull(message = "internshipId cannot be null")
    private Long internshipId;

    @NotBlank(message = "message cannot be blank")
    @Size(max = 500, message = "message cannot exceed 500 characters")
    private String message;

    @NotNull(message = "remindAt cannot be null")
    @Future(message = "remindAt must be in the future")
    private LocalDateTime remindAt;
}
