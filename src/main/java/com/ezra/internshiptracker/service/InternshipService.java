package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.dto.internship.InternshipStatusHistoryResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.InternshipStatusHistory;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.InternshipStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.exception.InvalidInternshipStatusTransitionException;
import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;

import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.repository.UserRepository;
import com.ezra.internshiptracker.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final InternshipStatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;

    public InternshipService(InternshipRepository internshipRepository,
                             InternshipStatusHistoryRepository statusHistoryRepository,
                             UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.userRepository = userRepository;
    }

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "company", "position", "status");

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

    public List<InternshipStatusHistoryResponse> getMyInternshipStatusHistory(Long id, Long userId) {
        internshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        return statusHistoryRepository.findByInternshipIdAndInternshipUserIdOrderByCreatedAtAsc(id, userId)
                .stream()
                .map(this::toStatusHistoryResponse)
                .toList();
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
        LocalDateTime now = LocalDateTime.now();
        internship.setCreatedAt(now);
        internship.setUpdatedAt(now);

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

    @Transactional
    public InternshipResponse updateInternship(Long id, UpdateInternshipRequest request, Long userId) {

        Internship internship = internshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        InternshipStatus currentStatus = internship.getStatus();
        InternshipStatus nextStatus = request.getStatus();

        validateStatusTransition(currentStatus, nextStatus);

        internship.setCompany(request.getCompany());
        internship.setPosition(request.getPosition());
        internship.setLocation(request.getLocation());
        internship.setStatus(nextStatus);
        internship.setApplicationUrl(request.getApplicationUrl());
        internship.setUpdatedAt(LocalDateTime.now());

        Internship updatedInternship = internshipRepository.save(internship);

        if (currentStatus != nextStatus) {
            saveStatusHistory(updatedInternship, currentStatus, nextStatus, userId, request.getStatusNote());
        }

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
        response.setUpdatedAt(internship.getUpdatedAt());

        return response;
    }

    private InternshipStatusHistoryResponse toStatusHistoryResponse(InternshipStatusHistory history) {
        InternshipStatusHistoryResponse response = new InternshipStatusHistoryResponse();

        response.setId(history.getId());
        response.setInternshipId(history.getInternship().getId());
        response.setFromStatus(history.getFromStatus());
        response.setToStatus(history.getToStatus());
        response.setNote(history.getNote());
        response.setCreatedAt(history.getCreatedAt());

        if (history.getOperator() != null) {
            response.setOperatorUserId(history.getOperator().getId());
            response.setOperatorUsername(history.getOperator().getUsername());
        }

        return response;
    }

    private void saveStatusHistory(
            Internship internship,
            InternshipStatus fromStatus,
            InternshipStatus toStatus,
            Long userId,
            String note
    ) {
        User operator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        InternshipStatusHistory history = new InternshipStatusHistory();
        history.setInternship(internship);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setOperator(operator);
        history.setNote(normalizeNote(note));
        history.setCreatedAt(LocalDateTime.now());

        statusHistoryRepository.save(history);
    }

    private void validateStatusTransition(
            InternshipStatus currentStatus,
            InternshipStatus nextStatus
    ) {
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new InvalidInternshipStatusTransitionException(currentStatus, nextStatus);
        }
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

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }

        return note.trim();
    }
}
