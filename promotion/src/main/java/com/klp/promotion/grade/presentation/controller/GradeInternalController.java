package com.klp.promotion.grade.presentation.controller;

import com.klp.promotion.grade.application.GradeService;
import com.klp.promotion.grade.presentation.dto.response.DefaultGradeResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/internal/promotions/grades")
@RequiredArgsConstructor
@Hidden
public class GradeInternalController {

    private final GradeService gradeService;

    /**
     * 기본 등급명을 조회합니다.
     */
    @GetMapping("/default")
    public ResponseEntity<DefaultGradeResponse> getDefaultGrade() {
        DefaultGradeResponse response = gradeService.getDefaultGradeName();
        return ResponseEntity.ok().body(response);
    }
}
