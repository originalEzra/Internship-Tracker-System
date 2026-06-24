package com.ezra.internshiptracker.exception;

import com.ezra.internshiptracker.entity.InternshipStatus;

public class InvalidInternshipStatusTransitionException extends RuntimeException {

    public InvalidInternshipStatusTransitionException(
            InternshipStatus currentStatus,
            InternshipStatus nextStatus
    ) {
        super("Cannot change internship status from " + currentStatus + " to " + nextStatus);
    }
}
