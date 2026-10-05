package com.nccaa.controller;

import com.nccaa.common.HealthStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight application-level health endpoint used by Render and clients.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public HealthStatus health() {
        return new HealthStatus("UP", "ncc-core-erp-backend");
    }

}
