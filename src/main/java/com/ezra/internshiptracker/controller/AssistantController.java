package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.ApiResponse;
import com.ezra.internshiptracker.dto.assistant.AssistantAdviceResponse;
import com.ezra.internshiptracker.service.AssistantService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    @GetMapping("/internships/{id}/advice")
    public ApiResponse<AssistantAdviceResponse> getInternshipAdvice(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        return ApiResponse.success(assistantService.getAdvice(id, userId));
    }
}
