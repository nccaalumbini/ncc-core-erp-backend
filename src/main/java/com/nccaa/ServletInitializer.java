package com.nccaa;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Enables the application to be deployed as a classic WAR to an external
 * servlet container while remaining a standalone Spring Boot application.
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(NccCoreErpBackendApplication.class);
    }

}
