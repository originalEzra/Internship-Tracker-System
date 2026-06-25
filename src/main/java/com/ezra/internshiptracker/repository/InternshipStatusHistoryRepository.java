package com.ezra.internshiptracker.repository;

import com.ezra.internshiptracker.entity.InternshipStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternshipStatusHistoryRepository extends JpaRepository<InternshipStatusHistory, Long> {

    List<InternshipStatusHistory> findByInternshipIdAndInternshipUserIdOrderByCreatedAtAsc(
            Long internshipId,
            Long userId
    );
}
