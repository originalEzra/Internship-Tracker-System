package com.ezra.internshiptracker.dto.internship;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateInternshipRequest {

    @NotBlank(message = "company cannot be blank")
    private String company;

    @NotBlank(message = "position cannot be blank")
    private String position;

    @NotBlank(message = "location cannot be blank")
    private String location;

    @NotBlank(message = "status cannot be blank")
    private String status;

    @NotBlank(message = "applicationUrl cannot be blank")
    private String applicationUrl;
}
