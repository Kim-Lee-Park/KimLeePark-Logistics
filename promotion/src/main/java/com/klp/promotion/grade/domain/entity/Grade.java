package com.klp.promotion.grade.domain.entity;

import com.klp.promotion.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Table(name = "p_grades", schema = "promotion_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Grade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grade_id")
    @Comment("등급 ID")
    private UUID gradeId;

    @Column(nullable = false, unique = true)
    @Comment("등급 이름")
    private String gradeName;

    @Column(nullable = false)
    @Comment("혜택 할인율")
    private Integer benefitDiscountRate;

    @Column(nullable = false)
    @Comment("해당 등급 최소 금액 범위")
    private Long minAmount;

    @Comment("해당 등급 최대 금액 범위")
    private Long maxAmount;

    public static Grade create(String gradeName, Integer benefitDiscountRate, Long minAmount, Long maxAmount) {
        Grade grade = new Grade();
        grade.gradeName = gradeName;
        grade.benefitDiscountRate = benefitDiscountRate;
        grade.minAmount = minAmount;
        grade.maxAmount = maxAmount;

        return grade;
    }

    public void update(String gradeName, Integer benefitDiscountRate, Long minAmount, Long maxAmount) {
        this.gradeName = gradeName;
        this.benefitDiscountRate = benefitDiscountRate;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }
}
