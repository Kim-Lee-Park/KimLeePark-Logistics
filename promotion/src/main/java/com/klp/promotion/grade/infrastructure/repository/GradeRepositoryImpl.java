package com.klp.promotion.grade.infrastructure.repository;

import com.klp.promotion.grade.domain.entity.Grade;
import com.klp.promotion.grade.domain.repository.GradeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GradeRepositoryImpl implements GradeRepository {

    private final GradeJpaRepository gradeJpaRepository;

    @Override
    public Grade save(Grade grade) {
        return gradeJpaRepository.save(grade);
    }

    @Override
    public Optional<Grade> findById(UUID gradeId) {
        return gradeJpaRepository.findById(gradeId);
    }

    @Override
    public List<Grade> findAll() {
        return gradeJpaRepository.findAll();
    }

    @Override
    public boolean existsByGradeName(String gradeName) {
        return gradeJpaRepository.existsByGradeName(gradeName);
    }
}
