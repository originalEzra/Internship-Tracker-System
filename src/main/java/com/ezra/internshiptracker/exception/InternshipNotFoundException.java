package com.ezra.internshiptracker.exception;

public class InternshipNotFoundException extends RuntimeException {

    public InternshipNotFoundException(String message) {
        super(message);
    }
}