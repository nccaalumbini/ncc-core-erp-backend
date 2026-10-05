package com.nccaa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating or updating a department.
 *
 * @param code        unique, short business identifier of the department
 * @param name        human-readable department name
 * @param description optional free-text description
 * @param active      whether the department is currently active
 */
public record DepartmentRequest(
        @NotBlank(message = "code is required")
        @Size(max = 32, message = "code must be at most 32 characters")
        String code,

        @NotBlank(message = "name is required")
        @Size(max = 150, message = "name must be at most 150 characters")
        String name,

        @Size(max = 500, message = "description must be at most 500 characters")
        String description,

        Boolean active
) {
}
