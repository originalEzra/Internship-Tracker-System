package com.ezra.internshiptracker.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPasswordUpdateRequest {

    @NotBlank(message = "Old password cannot be blank")
    private String oldPassword;

    @NotBlank(message = "New password cannot be blank")
    private String newPassword;
}
