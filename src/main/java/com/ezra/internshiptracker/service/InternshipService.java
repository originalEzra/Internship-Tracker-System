package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.repository.InternshipRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;

import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.repository.UserRepository;
import com.ezra.internshiptracker.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    public InternshipService(InternshipRepository internshipRepository,
                             UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "company", "position", "status");

    public PageResponse<InternshipResponse> getMyInternships(
            Long userId,
            int page,
            int size,
            InternshipStatus status,
            String keyword,
            String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        String normalizedKeyword = normalizeKeyword(keyword);

        Page<InternshipResponse> internships =
                internshipRepository.searchMyInternships(userId, status, normalizedKeyword, pageable)
                        .map(this::toResponse);

        return PageResponse.from(internships);
    }

    public InternshipResponse getMyInternshipById(Long id, Long userId) {
        Internship internship = internshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        return toResponse(internship);
    }

    public InternshipResponse createInternship(CreateInternshipRequest request, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Internship internship = new Internship();

        internship.setCompany(request.getCompany());
        internship.setPosition(request.getPosition());
        internship.setLocation(request.getLocation());
        internship.setStatus(request.getStatus());
        internship.setApplicationUrl(request.getApplicationUrl());
        internship.setCreatedAt(LocalDateTime.now());

        internship.setUser(user);

        Internship savedInternship = internshipRepository.save(internship);

        return toResponse(savedInternship);
    }

    public void deleteInternship(Long id, Long userId) {

        Internship internship = internshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new InternshipNotFoundException("Internship not found"));

        internshipRepository.delete(internship);
    }

    public InternshipResponse updateInternship(Long id, UpdateInternshipRequest request, Long userId) {

        Internship internship = internshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        internship.setCompany(request.getCompany());
        internship.setPosition(request.getPosition());
        internship.setLocation(request.getLocation());
        internship.setStatus(request.getStatus());
        internship.setApplicationUrl(request.getApplicationUrl());

        Internship updatedInternship = internshipRepository.save(internship);

        return toResponse(updatedInternship);
    }

    private InternshipResponse toResponse(Internship internship) {
        InternshipResponse response = new InternshipResponse();

        response.setId(internship.getId());
        response.setCompany(internship.getCompany());
        response.setPosition(internship.getPosition());
        response.setLocation(internship.getLocation());
        response.setStatus(internship.getStatus());
        response.setApplicationUrl(internship.getApplicationUrl());
        response.setCreatedAt(internship.getCreatedAt());

        return response;
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        return PageRequest.of(safePage, safeSize, parseSort(sort));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            field = "createdAt";
        }

        Sort.Direction direction = Sort.Direction.DESC;

        if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.ASC;
        }

        return Sort.by(direction, field);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return keyword.trim();
    }
}
