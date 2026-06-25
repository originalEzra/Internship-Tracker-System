package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.reminder.CreateReminderRequest;
import com.ezra.internshiptracker.dto.reminder.ReminderResponse;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.Reminder;
import com.ezra.internshiptracker.entity.ReminderStatus;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.exception.ReminderNotFoundException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final InternshipRepository internshipRepository;

    public ReminderService(ReminderRepository reminderRepository, InternshipRepository internshipRepository) {
        this.reminderRepository = reminderRepository;
        this.internshipRepository = internshipRepository;
    }

    public List<ReminderResponse> getMyReminders(Long userId, ReminderStatus status) {
        List<Reminder> reminders = status == null
                ? reminderRepository.findByUserIdOrderByRemindAtAsc(userId)
                : reminderRepository.findByUserIdAndStatusOrderByRemindAtAsc(userId, status);

        return reminders.stream()
                .map(this::toResponse)
                .toList();
    }

    public ReminderResponse createReminder(CreateReminderRequest request, Long userId) {
        Internship internship = internshipRepository.findByIdAndUserId(request.getInternshipId(), userId)
                .orElseThrow(() -> new InternshipNotFoundException("Internship not found"));

        LocalDateTime now = LocalDateTime.now();

        Reminder reminder = new Reminder();
        reminder.setUser(internship.getUser());
        reminder.setInternship(internship);
        reminder.setMessage(request.getMessage().trim());
        reminder.setRemindAt(request.getRemindAt());
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setCreatedAt(now);
        reminder.setUpdatedAt(now);

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public ReminderResponse cancelReminder(Long id, Long userId) {
        Reminder reminder = reminderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ReminderNotFoundException("Reminder not found"));

        reminder.setStatus(ReminderStatus.CANCELLED);
        reminder.setUpdatedAt(LocalDateTime.now());

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public int markDueRemindersAsSent(LocalDateTime now) {
        List<Reminder> dueReminders =
                reminderRepository.findByStatusAndRemindAtLessThanEqualOrderByRemindAtAsc(
                        ReminderStatus.PENDING,
                        now
                );

        dueReminders.forEach(reminder -> {
            reminder.setStatus(ReminderStatus.SENT);
            reminder.setUpdatedAt(now);
        });

        reminderRepository.saveAll(dueReminders);

        return dueReminders.size();
    }

    private ReminderResponse toResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();

        response.setId(reminder.getId());
        response.setInternshipId(reminder.getInternship().getId());
        response.setCompany(reminder.getInternship().getCompany());
        response.setPosition(reminder.getInternship().getPosition());
        response.setMessage(reminder.getMessage());
        response.setRemindAt(reminder.getRemindAt());
        response.setStatus(reminder.getStatus());
        response.setCreatedAt(reminder.getCreatedAt());
        response.setUpdatedAt(reminder.getUpdatedAt());

        return response;
    }
}
