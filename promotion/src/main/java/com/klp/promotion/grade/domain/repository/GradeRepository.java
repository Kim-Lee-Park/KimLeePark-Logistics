package com.klp.promotion.grade.domain.repository;

import com.klp.promotion.grade.domain.entity.Grade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository {

    Grade save(Grade grade);

    Optional<Grade> findById(UUID gradeId);
    
    List<Grade> findAll();

    boolean existsByGradeName(String gradeName);

    Optional<Grade> getGradeByName(String gradeName);
}
