package com.ezra.internshiptracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    private String message;

    private LocalDateTime remindAt;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
