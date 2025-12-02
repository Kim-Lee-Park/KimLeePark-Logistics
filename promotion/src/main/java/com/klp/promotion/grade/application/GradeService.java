package com.klp.promotion.grade.application;

import com.klp.common.exception.BusinessException;
import com.klp.promotion.grade.application.dto.CreateGradeCommand;
import com.klp.promotion.grade.application.dto.UpdateGradeCommand;
import com.klp.promotion.grade.domain.entity.Grade;
import com.klp.promotion.grade.domain.repository.GradeRepository;
import com.klp.promotion.grade.exception.GradeErrorCode;
import com.klp.promotion.grade.presentation.dto.response.GradeResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;

    @Transactional
    public GradeResponse createGrade(CreateGradeCommand command) {
        validateGradeNameDuplicate(command.gradeName());
        validateAmountRange(command.minAmount(), command.maxAmount());

        Grade grade = Grade.create(
            command.gradeName(),
            command.benefitDiscountRate(),
            command.minAmount(),
            command.maxAmount()
        );

        Grade savedGrade = gradeRepository.save(grade);
        log.info("등급 생성 완료: {}", savedGrade.getGradeId());

        return GradeResponse.from(savedGrade);
    }

    @Transactional(readOnly = true)
    public GradeResponse getGrade(UUID gradeId) {
        Grade grade = findGradeById(gradeId);
        return GradeResponse.from(grade);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getGrades() {
        List<Grade> gradeList = gradeRepository.findAll();

        return gradeList.stream()
            .map(GradeResponse::from)
            .toList();
    }

    @Transactional
    public GradeResponse updateGrade(UUID gradeId, UpdateGradeCommand command) {
        Grade grade = findGradeById(gradeId);

        if (!grade.getGradeName().equals(command.gradeName())) {
            validateGradeNameDuplicate(command.gradeName());
        }

        validateAmountRange(command.minAmount(), command.maxAmount());

        grade.update(
            command.gradeName(),
            command.benefitDiscountRate(),
            command.minAmount(),
            command.maxAmount()
        );

        log.info("등급 수정 완료: {}", gradeId);
        return GradeResponse.from(grade);
    }

    @Transactional
    public void deleteGrade(UUID gradeId, Long userId) {
        Grade grade = findGradeById(gradeId);
        grade.delete(userId);
        log.info("등급 삭제 완료: {}", gradeId);
    }

    private Grade findGradeById(UUID gradeId) {
        return gradeRepository.findById(gradeId)
            .orElseThrow(() -> new BusinessException(GradeErrorCode.GRADE_NOT_FOUND));
    }

    private void validateGradeNameDuplicate(String gradeName) {
        if (gradeRepository.existsByGradeName(gradeName)) {
            throw new BusinessException(GradeErrorCode.GRADE_NAME_DUPLICATE);
        }
    }

    private void validateAmountRange(Long minAmount, Long maxAmount) {
        if (minAmount != null && maxAmount != null && minAmount >= maxAmount) {
            throw new BusinessException(GradeErrorCode.INVALID_AMOUNT_RANGE);
        }
    }
}
