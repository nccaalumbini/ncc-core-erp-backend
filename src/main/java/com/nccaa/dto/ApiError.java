package com.nccaa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error payload returned by the API.
 *
 * <p>{@code validationErrors} is only present for validation failures; internal
 * implementation details (stack traces, SQL, credentials) are never included.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> validationErrors
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path,
                              List<FieldValidationError> validationErrors) {
        return new ApiError(Instant.now(), status, error, message, path, validationErrors);
    }

    /**
     * A single field-level validation failure.
     */
    public record FieldValidationError(String field, String message) {
    }

}
