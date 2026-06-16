package com.ezra.internshiptracker.dto.internship;

import com.ezra.internshiptracker.entity.InternshipStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateInternshipRequest {

    @NotBlank(message = "company cannot be blank")
    private String company;

    @NotBlank(message = "position cannot be blank")
    private String position;

    @NotBlank(message = "location cannot be blank")
    private String location;

    @NotNull(message = "status cannot be null")
    private InternshipStatus status;

    @NotBlank(message = "applicationUrl cannot be blank")
    private String applicationUrl;
}
