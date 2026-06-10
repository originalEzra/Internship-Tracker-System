package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.ApiResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.service.InternshipService;
import org.springframework.web.bind.annotation.*;

import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;

import org.springframework.security.core.Authentication;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/internships")
public class InternshipController {

    private final InternshipService internshipService;

    public InternshipController(InternshipService internshipService) {
        this.internshipService = internshipService;
    }

    private Long getCurrentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    @GetMapping
    public ApiResponse<List<InternshipResponse>> getMyInternships(
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        List<InternshipResponse> internships =
                internshipService.getMyInternships(userId);

        return ApiResponse.success(internships);
    }

    @GetMapping("/{id}")
    public ApiResponse<InternshipResponse> getInternshipById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        InternshipResponse internship =
                internshipService.getMyInternshipById(id, userId);

        return ApiResponse.success(internship);
    }

    @PostMapping
    public ApiResponse<InternshipResponse> createInternship(
            @Valid @RequestBody CreateInternshipRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        InternshipResponse internship =
                internshipService.createInternship(request, userId);

        return ApiResponse.success(internship);
    }

    @PutMapping("/{id}")
    public ApiResponse<InternshipResponse> updateInternship(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInternshipRequest request,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        InternshipResponse internship =
                internshipService.updateInternship(id, request, userId);

        return ApiResponse.success(internship);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInternship(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        internshipService.deleteInternship(id, userId);

        return ApiResponse.success(null);
    }
}
