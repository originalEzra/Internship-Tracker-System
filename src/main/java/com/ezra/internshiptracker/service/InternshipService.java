package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.repository.InternshipRepository;
import org.springframework.stereotype.Service;

import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;

import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.repository.UserRepository;
import com.ezra.internshiptracker.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;
    private final UserRepository userRepository;

    public InternshipService(InternshipRepository internshipRepository,
                             UserRepository userRepository) {
        this.internshipRepository = internshipRepository;
        this.userRepository = userRepository;
    }

    public List<InternshipResponse> getMyInternships(Long userId) {
        return internshipRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
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
}
