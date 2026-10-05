package com.nccaa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end HTTP checks against the full application context (web + actuator +
 * static resources), covering the behaviours that were previously broken.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HttpEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootReturns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("ncc-core-erp-backend"))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void applicationHealthReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("ncc-core-erp-backend"));
    }

    @Test
    void actuatorHealthReturns200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void nonexistentEndpointReturns404AsCleanJson() throws Exception {
        mockMvc.perform(get("/this/does/not/exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/this/does/not/exist"));
    }

    @Test
    void faviconIsServed() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isOk());
    }

}
