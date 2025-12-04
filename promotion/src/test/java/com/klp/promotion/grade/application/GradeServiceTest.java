package com.klp.promotion.grade.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.promotion.global.exception.BusinessException;
import com.klp.promotion.grade.application.dto.CreateGradeCommand;
import com.klp.promotion.grade.application.dto.UpdateGradeCommand;
import com.klp.promotion.grade.domain.entity.Grade;
import com.klp.promotion.grade.domain.repository.GradeRepository;
import com.klp.promotion.grade.exception.GradeErrorCode;
import com.klp.promotion.grade.presentation.dto.response.GradeResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeService 테스트")
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @InjectMocks
    private GradeService gradeService;

    private Grade testGrade;
    private UUID testGradeId;

    @BeforeEach
    void setUp() {
        testGradeId = UUID.randomUUID();
        testGrade = Grade.create("VIP", 15, 20000000L, 50000000L);
        ReflectionTestUtils.setField(testGrade, "gradeId", testGradeId);
    }

    @Nested
    @DisplayName("등급 생성 테스트")
    class CreateGradeTest {

        @Test
        @DisplayName("정상적으로 등급을 생성한다")
        void createGrade_Success() {
            // given
            CreateGradeCommand command = new CreateGradeCommand("VIP", 15, 20000000L, 50000000L);
            when(gradeRepository.existsByGradeName("VIP")).thenReturn(false);
            when(gradeRepository.save(any(Grade.class))).thenReturn(testGrade);

            // when
            GradeResponse response = gradeService.createGrade(command);

            // then
            assertNotNull(response);
            assertEquals("VIP", response.gradeName());
            assertEquals(15, response.benefitDiscountRate());
            assertEquals(20000000L, response.minAmount());
            assertEquals(50000000L, response.maxAmount());
            verify(gradeRepository, times(1)).existsByGradeName("VIP");
            verify(gradeRepository, times(1)).save(any(Grade.class));
        }

        @Test
        @DisplayName("중복된 등급 이름으로 생성 시 예외가 발생한다")
        void createGrade_DuplicateName_ThrowsException() {
            // given
            CreateGradeCommand command = new CreateGradeCommand("VIP", 15, 20000000L, 50000000L);
            when(gradeRepository.existsByGradeName("VIP")).thenReturn(true);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(command));
            assertEquals(GradeErrorCode.GRADE_NAME_DUPLICATE, exception.getErrorCode());
            verify(gradeRepository, times(1)).existsByGradeName("VIP");
            verify(gradeRepository, times(0)).save(any(Grade.class));
        }

        @Test
        @DisplayName("최소 금액이 최대 금액보다 크거나 같으면 예외가 발생한다")
        void createGrade_InvalidAmountRange_ThrowsException() {
            // given
            CreateGradeCommand command = new CreateGradeCommand("VIP", 15, 50000000L, 20000000L);
            when(gradeRepository.existsByGradeName("VIP")).thenReturn(false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(command));
            assertEquals(GradeErrorCode.INVALID_AMOUNT_RANGE, exception.getErrorCode());
            verify(gradeRepository, times(0)).save(any(Grade.class));
        }

        @Test
        @DisplayName("최소 금액과 최대 금액이 같으면 예외가 발생한다")
        void createGrade_EqualAmounts_ThrowsException() {
            // given
            CreateGradeCommand command = new CreateGradeCommand("VIP", 15, 30000000L, 30000000L);
            when(gradeRepository.existsByGradeName("VIP")).thenReturn(false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.createGrade(command));
            assertEquals(GradeErrorCode.INVALID_AMOUNT_RANGE, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("등급 조회 테스트")
    class GetGradeTest {

        @Test
        @DisplayName("ID로 등급을 정상적으로 조회한다")
        void getGrade_Success() {
            // given
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));

            // when
            GradeResponse response = gradeService.getGrade(testGradeId);

            // then
            assertNotNull(response);
            assertEquals("VIP", response.gradeName());
            assertEquals(15, response.benefitDiscountRate());
            verify(gradeRepository, times(1)).findById(testGradeId);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
        void getGrade_NotFound_ThrowsException() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            when(gradeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.getGrade(nonExistentId));
            assertEquals(GradeErrorCode.GRADE_NOT_FOUND, exception.getErrorCode());
            verify(gradeRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("등급 목록 조회 테스트")
    class GetGradesTest {

        @Test
        @DisplayName("모든 등급 목록을 정상적으로 조회한다")
        void getGrades_Success() {
            // given
            Grade grade1 = Grade.create("Bronze", 4, 1000000L, 5000000L);
            Grade grade2 = Grade.create("Silver", 7, 5000000L, 10000000L);
            Grade grade3 = Grade.create("Gold", 10, 10000000L, 50000000L);
            List<Grade> grades = Arrays.asList(grade1, grade2, grade3);

            when(gradeRepository.findAll()).thenReturn(grades);

            // when
            List<GradeResponse> responses = gradeService.getGrades();

            // then
            assertNotNull(responses);
            assertEquals(3, responses.size());
            assertEquals("Bronze", responses.get(0).gradeName());
            assertEquals("Silver", responses.get(1).gradeName());
            assertEquals("Gold", responses.get(2).gradeName());
            verify(gradeRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("등급이 없을 때 빈 목록을 반환한다")
        void getGrades_EmptyList() {
            // given
            when(gradeRepository.findAll()).thenReturn(List.of());

            // when
            List<GradeResponse> responses = gradeService.getGrades();

            // then
            assertNotNull(responses);
            assertEquals(0, responses.size());
            verify(gradeRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("등급 수정 테스트")
    class UpdateGradeTest {

        @Test
        @DisplayName("등급을 정상적으로 수정한다")
        void updateGrade_Success() {
            // given
            UpdateGradeCommand command = new UpdateGradeCommand("VVIP", 20, 50000000L, 100000000L);
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));
            when(gradeRepository.existsByGradeName("VVIP")).thenReturn(false);

            // when
            GradeResponse response = gradeService.updateGrade(testGradeId, command);

            // then
            assertNotNull(response);
            assertEquals("VVIP", response.gradeName());
            assertEquals(20, response.benefitDiscountRate());
            assertEquals(50000000L, response.minAmount());
            assertEquals(100000000L, response.maxAmount());
            verify(gradeRepository, times(1)).findById(testGradeId);
            verify(gradeRepository, times(1)).existsByGradeName("VVIP");
        }

        @Test
        @DisplayName("같은 이름으로 수정할 때는 중복 체크를 하지 않는다")
        void updateGrade_SameName_SkipsDuplicateCheck() {
            // given
            UpdateGradeCommand command = new UpdateGradeCommand("VIP", 20, 50000000L, 100000000L);
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));

            // when
            GradeResponse response = gradeService.updateGrade(testGradeId, command);

            // then
            assertNotNull(response);
            assertEquals("VIP", response.gradeName());
            assertEquals(20, response.benefitDiscountRate());
            verify(gradeRepository, times(1)).findById(testGradeId);
            verify(gradeRepository, times(0)).existsByGradeName(any());
        }

        @Test
        @DisplayName("다른 등급에서 사용 중인 이름으로 수정 시 예외가 발생한다")
        void updateGrade_DuplicateName_ThrowsException() {
            // given
            UpdateGradeCommand command = new UpdateGradeCommand("Platinum", 25, 50000000L,
                100000000L);
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));
            when(gradeRepository.existsByGradeName("Platinum")).thenReturn(true);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.updateGrade(testGradeId, command));
            assertEquals(GradeErrorCode.GRADE_NAME_DUPLICATE, exception.getErrorCode());
            verify(gradeRepository, times(1)).existsByGradeName("Platinum");
        }

        @Test
        @DisplayName("잘못된 금액 범위로 수정 시 예외가 발생한다")
        void updateGrade_InvalidAmountRange_ThrowsException() {
            // given
            UpdateGradeCommand command = new UpdateGradeCommand("VIP", 20, 100000000L, 50000000L);
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.updateGrade(testGradeId, command));
            assertEquals(GradeErrorCode.INVALID_AMOUNT_RANGE, exception.getErrorCode());
        }

        @Test
        @DisplayName("존재하지 않는 등급을 수정하려고 할 때 예외가 발생한다")
        void updateGrade_NotFound_ThrowsException() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            UpdateGradeCommand command = new UpdateGradeCommand("VVIP", 20, 50000000L, 100000000L);
            when(gradeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.updateGrade(nonExistentId, command));
            assertEquals(GradeErrorCode.GRADE_NOT_FOUND, exception.getErrorCode());
            verify(gradeRepository, times(1)).findById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("등급 삭제 테스트")
    class DeleteGradeTest {

        @Test
        @DisplayName("등급을 정상적으로 삭제한다")
        void deleteGrade_Success() {
            // given
            Long userId = 1L;
            when(gradeRepository.findById(testGradeId)).thenReturn(Optional.of(testGrade));

            // when
            gradeService.deleteGrade(testGradeId, userId);

            // then
            verify(gradeRepository, times(1)).findById(testGradeId);
        }

        @Test
        @DisplayName("존재하지 않는 등급을 삭제하려고 할 때 예외가 발생한다")
        void deleteGrade_NotFound_ThrowsException() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            Long userId = 1L;
            when(gradeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> gradeService.deleteGrade(nonExistentId, userId));
            assertEquals(GradeErrorCode.GRADE_NOT_FOUND, exception.getErrorCode());
            verify(gradeRepository, times(1)).findById(nonExistentId);
        }
    }
}
