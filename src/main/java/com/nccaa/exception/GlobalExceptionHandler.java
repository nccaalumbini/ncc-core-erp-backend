package com.nccaa.exception;

import com.nccaa.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/**
 * Centralised translation of exceptions into consistent, non-leaking JSON errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), request.getRequestURI());
    }

    /**
     * A request for a path that has no controller mapping and no static resource,
     * e.g. {@code GET /} when no root handler exists, or {@code GET /favicon.ico}
     * when no favicon is bundled. This is an expected client error, not a server
     * fault, so it must be a 404 and must not be logged at ERROR level.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("No static resource or handler for '{}'", request.getRequestURI());
        return ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiError.FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Validation failed for one or more fields", request.getRequestURI(), errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.FieldValidationError> errors = ex.getConstraintViolations().stream()
                .map(this::toFieldError)
                .toList();
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Validation failed for one or more fields", request.getRequestURI(), errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadableMessage(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Malformed or unreadable request body", request.getRequestURI());
    }

    /**
     * A path variable or request parameter that cannot be converted to its target
     * type, e.g. {@code /api/v1/departments/id=1} where a numeric id is expected.
     * This is a client error, so it must be a 400, not a 500.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Class<?> requiredType = ex.getRequiredType();
        String expected = (requiredType != null) ? requiredType.getSimpleName() : "the expected type";
        return ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Invalid value for '" + ex.getName() + "': expected " + expected, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {}", request.getRequestURI());
        return ApiError.of(HttpStatus.CONFLICT.value(), "Conflict",
                "The request conflicts with an existing record or database constraint",
                request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An unexpected error occurred", request.getRequestURI());
    }

    private ApiError.FieldValidationError toFieldError(ConstraintViolation<?> violation) {
        return new ApiError.FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage());
    }

}
