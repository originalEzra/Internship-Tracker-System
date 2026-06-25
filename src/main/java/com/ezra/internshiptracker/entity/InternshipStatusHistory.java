package com.ezra.internshiptracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "internship_status_history")
public class InternshipStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    @Enumerated(EnumType.STRING)
    private InternshipStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private InternshipStatus toStatus;

    @ManyToOne
    @JoinColumn(name = "operator_user_id")
    private User operator;

    private String note;

    private LocalDateTime createdAt;
}
