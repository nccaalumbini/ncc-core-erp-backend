package com.nccaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the NCCAA Core ERP backend.
 *
 * <p>This is the foundation of a modular ERP system. The Department module is the
 * reference implementation that future modules should follow.</p>
 */
@SpringBootApplication
public class NccCoreErpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NccCoreErpBackendApplication.class, args);
    }

}
