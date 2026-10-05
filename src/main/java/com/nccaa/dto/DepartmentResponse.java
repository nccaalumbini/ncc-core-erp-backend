package com.nccaa.dto;

import java.time.Instant;

/**
 * Response payload representing a department.
 *
 * @param id          generated identifier
 * @param code        unique business identifier
 * @param name        human-readable name
 * @param description free-text description
 * @param active      whether the department is currently active
 * @param createdAt   creation timestamp (UTC)
 * @param updatedAt   last modification timestamp (UTC)
 */
public record DepartmentResponse(
        Long id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
