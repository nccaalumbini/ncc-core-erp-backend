package com.nccaa.mapper;

import com.nccaa.dto.DepartmentResponse;
import com.nccaa.entity.DepartmentEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between the department persistence model and its API representation.
 */
@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(DepartmentEntity entity) {
        return new DepartmentResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
