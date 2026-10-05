package com.nccaa.repository;

import com.nccaa.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for departments.
 *
 * <p>All access goes through parameterized Spring Data derived queries; no
 * SQL strings are concatenated anywhere in the application.</p>
 */
@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    Optional<DepartmentEntity> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<DepartmentEntity> findAllByOrderByNameAsc();

}
