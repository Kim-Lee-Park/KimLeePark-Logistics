package com.klp.user.domain.entity;

import com.klp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(
    name = "p_user_grades",
    indexes = {
        @Index(
            name = "idx_user_grade_user_evaluated",
            columnList = "user_id, evaluated_at DESC"
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGrade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_grade_id")
    @Comment("회원등급 ID")
    private UUID userGradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("유저ID")
    private User user;

    @Column(name = "grade_name", nullable = false)
    @Comment("등급명")
    private String gradeName;

    @Column(name = "evaluated_at")
    @Comment("등급 변경 시간")
    private LocalDateTime evaluatedAt;

    private UserGrade(User user, String gradeName) {
        this.user = user;
        this.gradeName = gradeName;
        this.evaluatedAt = LocalDateTime.now();
    }

    public static UserGrade create(User user, String gradeName) {
        return new UserGrade(user, gradeName);
    }
}
