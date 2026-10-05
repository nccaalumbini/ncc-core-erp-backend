package com.nccaa.controller;

import com.nccaa.common.ApplicationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root endpoint identifying the running service.
 *
 * <p>Kept separate from the business APIs and from infrastructure endpoints so the
 * application can always answer {@code GET /} even before any domain data exists.
 * Render health checks should still target {@code /actuator/health}.</p>
 */
@RestController
public class RootController {

    private final String applicationName;
    private final String version;

    public RootController(@Value("${spring.application.name:ncc-core-erp-backend}") String applicationName,
                          @Value("${application.version:0.0.1-SNAPSHOT}") String version) {
        this.applicationName = applicationName;
        this.version = version;
    }

    @GetMapping("/")
    public ApplicationInfo root() {
        return new ApplicationInfo(
                applicationName,
                "UP",
                "NCC Core ERP Backend is running",
                version
        );
    }

}
