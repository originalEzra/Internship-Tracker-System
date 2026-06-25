package com.ezra.internshiptracker.exception;

import com.ezra.internshiptracker.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<String>> error(int status, String message) {
        ApiResponse<String> response = new ApiResponse<>(status, message, null);

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return error(400, message);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserNotFoundException(
            UserNotFoundException e
    ) {
        return error(404, e.getMessage());
    }

    @ExceptionHandler(InternshipNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleInternshipNotFound(
            InternshipNotFoundException e
    ) {
        return error(404, e.getMessage());
    }

    @ExceptionHandler(ReminderNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleReminderNotFound(
            ReminderNotFoundException e
    ) {
        return error(404, e.getMessage());
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotificationNotFound(
            NotificationNotFoundException e
    ) {
        return error(404, e.getMessage());
    }

    @ExceptionHandler(InvalidInternshipStatusTransitionException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidInternshipStatusTransitionException(
            InvalidInternshipStatusTransitionException e
    ) {
        return error(400, e.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ApiResponse<String>> handleDuplicateUserException(
            DuplicateUserException e
    ) {
        return error(400, e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidPasswordException(
            InvalidPasswordException e
    ) {
        return error(400, e.getMessage());
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ApiResponse<String>> handleLoginFailedException(
            LoginFailedException e
    ) {
        return error(401, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedException(
            UnauthorizedException e
    ) {
        return error(401, e.getMessage());
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ApiResponse<String>> handleTooManyLoginAttemptsException(
            TooManyLoginAttemptsException e
    ) {
        return error(429, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ) {
        return error(400, "Duplicate or invalid data");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ) {
        return error(400, "Invalid request parameter");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        return error(400, "Invalid request body");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        return error(500, "Internal server error");
    }
}
