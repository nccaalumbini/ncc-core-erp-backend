package com.nccaa.common;

/**
 * Identification payload returned by {@code GET /}.
 *
 * @param application application name
 * @param status      current status
 * @param message     human-readable message
 * @param version     build version
 */
public record ApplicationInfo(String application, String status, String message, String version) {
}
