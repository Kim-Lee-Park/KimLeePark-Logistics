package com.klp.promotion.grade.presentation.controller;

import com.klp.promotion.global.security.model.UserDetailsImpl;
import com.klp.promotion.grade.application.GradeService;
import com.klp.promotion.grade.presentation.dto.request.CreateGradeRequest;
import com.klp.promotion.grade.presentation.dto.request.UpdateGradeRequest;
import com.klp.promotion.grade.presentation.dto.response.GradeResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/promotions/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<GradeResponse> createGrade(@Valid @RequestBody CreateGradeRequest request) {
        GradeResponse response = gradeService.createGrade(request.toCommand());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gradeId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<GradeResponse> getGrade(@PathVariable UUID gradeId) {
        GradeResponse response = gradeService.getGrade(gradeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<List<GradeResponse>> getGrades() {
        List<GradeResponse> response = gradeService.getGrades();
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{gradeId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<GradeResponse> updateGrade(
        @PathVariable UUID gradeId,
        @Valid @RequestBody UpdateGradeRequest request
    ) {
        GradeResponse response = gradeService.updateGrade(gradeId, request.toCommand());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{gradeId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteGrade(
        @PathVariable UUID gradeId,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        gradeService.deleteGrade(gradeId, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}
