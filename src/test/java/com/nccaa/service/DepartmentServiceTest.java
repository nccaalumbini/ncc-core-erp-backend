package com.nccaa.service;

import com.nccaa.dto.DepartmentRequest;
import com.nccaa.dto.DepartmentResponse;
import com.nccaa.exception.DuplicateResourceException;
import com.nccaa.exception.ResourceNotFoundException;
import com.nccaa.mapper.DepartmentMapper;
import com.nccaa.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DepartmentServiceTest {

    @Autowired
    private DepartmentService service;

    @Autowired
    private DepartmentRepository repository;

    @Autowired
    private DepartmentMapper mapper;

    private DepartmentRequest request(String code, String name, String description, Boolean active) {
        return new DepartmentRequest(code, name, description, active);
    }

    @Test
    void createPersistsDepartmentAndNormalizesCode() {
        DepartmentResponse response = service.create(request("  hr ", "Human Resources", "People", true));

        assertThat(response.id()).isNotNull();
        assertThat(response.code()).isEqualTo("HR");
        assertThat(response.name()).isEqualTo("Human Resources");
        assertThat(response.active()).isTrue();
        assertThat(repository.findById(response.id())).isPresent();
    }

    @Test
    void createDefaultsActiveToTrueWhenNull() {
        DepartmentResponse response = service.create(request("OPS", "Operations", null, null));

        assertThat(response.active()).isTrue();
        assertThat(response.description()).isNull();
    }

    @Test
    void createRejectsDuplicateCodeIgnoringCase() {
        service.create(request("FIN", "Finance", null, true));

        assertThatThrownBy(() -> service.create(request("fin", "Finance Duplicate", null, true)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void findAllReturnsDepartmentsOrderedByName() {
        service.create(request("B", "Beta", null, true));
        service.create(request("A", "Alpha", null, true));

        List<DepartmentResponse> all = service.findAll();

        assertThat(all).extracting(DepartmentResponse::name).containsExactly("Alpha", "Beta");
    }

    @Test
    void findByIdReturnsDepartment() {
        DepartmentResponse created = service.create(request("IT", "IT", null, true));

        DepartmentResponse found = service.findById(created.id());

        assertThat(found).isEqualTo(created);
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThatThrownBy(() -> service.findById(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    void updateChangesFields() {
        DepartmentResponse created = service.create(request("HR", "Human Resources", "Old", true));

        DepartmentResponse updated = service.update(created.id(),
                request("hr", "People Team", "New", false));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("People Team");
        assertThat(updated.description()).isEqualTo("New");
        assertThat(updated.active()).isFalse();
    }

    @Test
    void updateThrowsWhenMissing() {
        assertThatThrownBy(() -> service.update(9999L, request("XX", "None", null, true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRejectsCodeUsedByAnotherDepartment() {
        service.create(request("AAA", "First", null, true));
        DepartmentResponse second = service.create(request("BBB", "Second", null, true));

        assertThatThrownBy(() -> service.update(second.id(), request("aaa", "Second", null, true)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateKeepsActiveWhenRequestOmitsIt() {
        DepartmentResponse created = service.create(request("HR", "Human Resources", null, true));

        DepartmentResponse updated = service.update(created.id(), request("HR", "Human Resources v2", null, null));

        assertThat(updated.active()).isTrue();
    }

    @Test
    void updateRefreshesUpdatedAt() throws InterruptedException {
        DepartmentResponse created = service.create(request("TS", "Timestamps", null, true));

        Thread.sleep(5);
        DepartmentResponse updated = service.update(created.id(), request("TS", "Timestamps v2", null, true));

        assertThat(updated.updatedAt()).isAfter(updated.createdAt());
    }

    @Test
    void deleteRemovesDepartment() {
        DepartmentResponse created = service.create(request("DEL", "To Delete", null, true));

        service.delete(created.id());

        assertThat(repository.findById(created.id())).isEmpty();
    }

    @Test
    void deleteThrowsWhenMissing() {
        assertThatThrownBy(() -> service.delete(9999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
