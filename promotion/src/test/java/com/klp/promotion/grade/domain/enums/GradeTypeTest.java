package com.klp.promotion.grade.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("GradeType 테스트")
class GradeTypeTest {

    @Test
    @DisplayName("null 금액은 NONE 등급을 반환한다")
    void calculateGrade_NullAmount_ReturnsNone() {
        // when
        GradeType result = GradeType.calculateGrade(null);

        // then
        assertEquals(GradeType.NONE, result);
    }

    @Test
    @DisplayName("음수 금액은 NONE 등급을 반환한다")
    void calculateGrade_NegativeAmount_ReturnsNone() {
        // when
        GradeType result = GradeType.calculateGrade(-1000L);

        // then
        assertEquals(GradeType.NONE, result);
    }

    @ParameterizedTest
    @CsvSource({
        "0, NONE",
        "500000, NONE",
        "1000000, NONE"
    })
    @DisplayName("NONE 등급 범위 테스트 (0 ~ 1,000,000)")
    void calculateGrade_NoneRange(Long amount, GradeType expected) {
        // when
        GradeType result = GradeType.calculateGrade(amount);

        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "1010000, BRONZE",
        "2000000, BRONZE",
        "5000000, BRONZE"
    })
    @DisplayName("BRONZE 등급 범위 테스트 (1,010,000 ~ 5,000,000)")
    void calculateGrade_BronzeRange(Long amount, GradeType expected) {
        // when
        GradeType result = GradeType.calculateGrade(amount);

        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "5010000, SILVER",
        "7000000, SILVER",
        "10000000, SILVER"
    })
    @DisplayName("SILVER 등급 범위 테스트 (5,010,000 ~ 10,000,000)")
    void calculateGrade_SilverRange(Long amount, GradeType expected) {
        // when
        GradeType result = GradeType.calculateGrade(amount);

        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "10010000, GOLD",
        "50000000, GOLD",
        "100000000, GOLD"
    })
    @DisplayName("GOLD 등급 범위 테스트 (10,010,000 이상)")
    void calculateGrade_GoldRange(Long amount, GradeType expected) {
        // when
        GradeType result = GradeType.calculateGrade(amount);

        // then
        assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
        "1000000, NONE",
        "1010000, BRONZE",
        "5000000, BRONZE",
        "5010000, SILVER",
        "10000000, SILVER",
        "10010000, GOLD"
    })
    @DisplayName("등급 경계값 테스트")
    void calculateGrade_BoundaryValues(Long amount, GradeType expected) {
        // when
        GradeType result = GradeType.calculateGrade(amount);

        // then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("각 등급의 할인율이 올바르게 설정되어 있다")
    void gradeType_HasCorrectDiscountRates() {
        // then
        assertEquals(0, GradeType.NONE.getDiscountRate());
        assertEquals(4, GradeType.BRONZE.getDiscountRate());
        assertEquals(7, GradeType.SILVER.getDiscountRate());
        assertEquals(10, GradeType.GOLD.getDiscountRate());
    }

    @Test
    @DisplayName("각 등급의 표시 이름이 올바르게 설정되어 있다")
    void gradeType_HasCorrectDisplayNames() {
        // then
        assertEquals("등급 없음", GradeType.NONE.getDisplayName());
        assertEquals("브론즈", GradeType.BRONZE.getDisplayName());
        assertEquals("실버", GradeType.SILVER.getDisplayName());
        assertEquals("골드", GradeType.GOLD.getDisplayName());
    }
}
