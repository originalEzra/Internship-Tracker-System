package com.ezra.internshiptracker.repository;

import com.ezra.internshiptracker.entity.Notification;
import com.ezra.internshiptracker.entity.NotificationSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    boolean existsBySourceTypeAndSourceId(NotificationSourceType sourceType, Long sourceId);
}

