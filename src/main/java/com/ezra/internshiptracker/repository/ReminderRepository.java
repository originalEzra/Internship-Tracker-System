package com.ezra.internshiptracker.repository;

import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    Optional<Reminder> findByIdAndUserId(Long id, Long userId);

    List<Reminder> findByUserIdOrderByRemindAtAsc(Long userId);

    List<Reminder> findByUserIdAndStatusOrderByRemindAtAsc(Long userId, ReminderStatus status);

    List<Reminder> findByInternshipIdAndUserIdAndStatusOrderByRemindAtAsc(
            Long internshipId,
            Long userId,
            ReminderStatus status
    );

    List<Reminder> findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
            ReminderStatus status,
            LocalDateTime remindAt
    );
}
