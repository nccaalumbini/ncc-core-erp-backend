package com.nccaa.repository;

import com.nccaa.entity.DepartmentEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DepartmentRepositoryTest {

    @Autowired
    private DepartmentRepository repository;

    @Test
    void savesAndAssignsGeneratedIdAndTimestamps() {
        DepartmentEntity saved = repository.save(
                new DepartmentEntity("HR", "Human Resources", "People operations", true));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void findsByCodeIgnoringCase() {
        repository.save(new DepartmentEntity("FIN", "Finance", null, true));

        Optional<DepartmentEntity> found = repository.findByCodeIgnoreCase("fin");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Finance");
    }

    @Test
    void reportsExistingCodeIgnoringCase() {
        repository.save(new DepartmentEntity("IT", "Information Technology", null, true));

        assertThat(repository.existsByCodeIgnoreCase("it")).isTrue();
        assertThat(repository.existsByCodeIgnoreCase("nope")).isFalse();
    }

    @Test
    void listsDepartmentsOrderedByName() {
        repository.save(new DepartmentEntity("B", "Beta", null, true));
        repository.save(new DepartmentEntity("A", "Alpha", null, true));

        List<DepartmentEntity> all = repository.findAllByOrderByNameAsc();

        assertThat(all).extracting(DepartmentEntity::getName).containsExactly("Alpha", "Beta");
    }

}
