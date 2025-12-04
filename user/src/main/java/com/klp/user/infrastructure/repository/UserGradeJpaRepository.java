package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.UserGrade;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGradeJpaRepository extends JpaRepository<UserGrade, UUID> {

    Optional<UserGrade> findFirstByUser_UserIdOrderByEvaluatedAtDesc(Long userId);
}
