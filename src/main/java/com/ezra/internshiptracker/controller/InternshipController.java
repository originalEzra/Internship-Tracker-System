package com.ezra.internshiptracker.controller;

import com.ezra.internshiptracker.dto.ApiResponse;
import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.dto.internship.InternshipStatusHistoryResponse;
import com.ezra.internshiptracker.entity.InternshipStatus;
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
    public ApiResponse<PageResponse<InternshipResponse>> getMyInternships(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) InternshipStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Long userId = getCurrentUserId(authentication);

        PageResponse<InternshipResponse> internships =
                internshipService.getMyInternships(userId, page, size, status, keyword, sort);

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

    @GetMapping("/{id}/status-history")
    public ApiResponse<List<InternshipStatusHistoryResponse>> getInternshipStatusHistory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);

        List<InternshipStatusHistoryResponse> history =
                internshipService.getMyInternshipStatusHistory(id, userId);

        return ApiResponse.success(history);
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
