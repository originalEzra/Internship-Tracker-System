package com.ezra.internshiptracker.exception;

public class ReminderNotFoundException extends RuntimeException {

    public ReminderNotFoundException(Long id) {
        super("Reminder not found with id: " + id);
    }

    public ReminderNotFoundException(String message) {
        super(message);
    }
}
