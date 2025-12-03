package com.klp.promotion.grade.infrastructure.repository;

import com.klp.promotion.grade.domain.entity.Grade;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeJpaRepository extends JpaRepository<Grade, UUID> {

    Optional<Grade> findByGradeName(String gradeName);

    boolean existsByGradeName(String gradeName);
}
