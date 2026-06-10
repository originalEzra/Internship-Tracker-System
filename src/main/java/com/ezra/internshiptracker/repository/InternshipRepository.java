package com.ezra.internshiptracker.repository;

import com.ezra.internshiptracker.entity.Internship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternshipRepository extends JpaRepository<Internship, Long> {

    List<Internship> findByUserId(Long userId);

    Optional<Internship> findByIdAndUserId(Long id, Long userId);
}
