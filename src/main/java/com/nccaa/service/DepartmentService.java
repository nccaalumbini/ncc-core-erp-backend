package com.nccaa.service;

import com.nccaa.dto.DepartmentRequest;
import com.nccaa.dto.DepartmentResponse;
import com.nccaa.entity.DepartmentEntity;
import com.nccaa.exception.DuplicateResourceException;
import com.nccaa.exception.ResourceNotFoundException;
import com.nccaa.mapper.DepartmentMapper;
import com.nccaa.repository.DepartmentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application logic for managing departments.
 */
@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;

    public DepartmentService(DepartmentRepository repository, DepartmentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<DepartmentResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public DepartmentResponse findById(Long id) {
        return mapper.toResponse(getEntity(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String code = normalizeCode(request.code());
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Department code already exists: " + code);
        }
        DepartmentEntity entity = new DepartmentEntity(
                code,
                request.name().trim(),
                trimToNull(request.description()),
                request.active() == null || request.active()
        );
        // saveAndFlush so lifecycle callbacks populate timestamps before mapping.
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        DepartmentEntity entity = getEntity(id);
        String code = normalizeCode(request.code());

        repository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Department code already exists: " + code);
                });

        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setDescription(trimToNull(request.description()));
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        // saveAndFlush so @PreUpdate runs before the response DTO is mapped and
        // the returned updatedAt reflects this change.
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        DepartmentEntity entity = getEntity(id);
        repository.delete(entity);
    }

    private DepartmentEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
