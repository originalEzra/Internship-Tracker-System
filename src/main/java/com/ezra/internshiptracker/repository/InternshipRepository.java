package com.ezra.internshiptracker.repository;

import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InternshipRepository extends JpaRepository<Internship, Long> {

    Optional<Internship> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT i
            FROM Internship i
            WHERE i.user.id = :userId
              AND (:status IS NULL OR i.status = :status)
              AND (
                    :keyword IS NULL
                    OR LOWER(i.company) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(i.position) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Internship> searchMyInternships(
            @Param("userId") Long userId,
            @Param("status") InternshipStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
