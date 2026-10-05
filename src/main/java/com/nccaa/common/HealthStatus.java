package com.nccaa.common;

/**
 * Simple health payload returned by {@code GET /api/v1/health}.
 */
public record HealthStatus(String status, String application) {
}
