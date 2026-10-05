package com.nccaa.controller;

import com.nccaa.dto.DepartmentResponse;
import com.nccaa.exception.GlobalExceptionHandler;
import com.nccaa.exception.ResourceNotFoundException;
import com.nccaa.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService service;

    private DepartmentResponse sample(Long id, String code, String name, boolean active) {
        return new DepartmentResponse(id, code, name, "desc", active, Instant.now(), Instant.now());
    }

    private static final String VALID_BODY = """
            {"code":"HR","name":"Human Resources","description":"desc","active":true}
            """;

    @Test
    void getReturnsAllDepartments() throws Exception {
        given(service.findAll()).willReturn(List.of(
                sample(1L, "HR", "Human Resources", true),
                sample(2L, "FIN", "Finance", true)));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("HR"))
                .andExpect(jsonPath("$[1].code").value("FIN"));
    }

    @Test
    void getByIdReturnsDepartment() throws Exception {
        given(service.findById(1L)).willReturn(sample(1L, "HR", "Human Resources", true));

        mockMvc.perform(get("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Human Resources"));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        given(service.findById(99L)).willThrow(new ResourceNotFoundException("Department not found with id: 99"));

        mockMvc.perform(get("/api/v1/departments/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Department not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/v1/departments/99"));
    }

    @Test
    void postCreatesDepartmentAndReturns201WithLocation() throws Exception {
        given(service.create(any())).willReturn(sample(5L, "HR", "Human Resources", true));

        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/departments/5")))
                .andExpect(jsonPath("$.id").value(5));

        verify(service).create(any());
    }

    @Test
    void postReturns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"","name":"","active":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field", containsInAnyOrder("code", "name")));
    }

    @Test
    void putUpdatesDepartment() throws Exception {
        given(service.update(eq(3L), any()))
                .willReturn(sample(3L, "OPS", "Operations", false));

        mockMvc.perform(put("/api/v1/departments/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"OPS","name":"Operations","active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OPS"))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void putReturns404WhenMissing() throws Exception {
        given(service.update(eq(99L), any()))
                .willThrow(new ResourceNotFoundException("Department not found with id: 99"));

        mockMvc.perform(put("/api/v1/departments/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/departments/7"))
                .andExpect(status().isNoContent());

        verify(service).delete(7L);
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        willThrow(new ResourceNotFoundException("Department not found with id: 7"))
                .given(service).delete(7L);

        mockMvc.perform(delete("/api/v1/departments/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unexpectedExceptionReturns500WithoutLeakingDetails() throws Exception {
        given(service.findById(42L)).willThrow(new RuntimeException("secret internal detail"));

        mockMvc.perform(get("/api/v1/departments/42"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("secret"))));
    }

    @Test
    void nonNumericPathVariableReturns400NotServerError() throws Exception {
        mockMvc.perform(get("/api/v1/departments/id=1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("id")));
    }

}
