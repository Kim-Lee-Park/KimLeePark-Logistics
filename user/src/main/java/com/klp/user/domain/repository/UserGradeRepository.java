package com.klp.user.domain.repository;

import com.klp.user.domain.entity.UserGrade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGradeRepository extends JpaRepository<UserGrade, UUID> {

    Optional<UserGrade> findFirstByUser_UserIdOrderByEvaluatedAtDesc(Long userId);

    List<UserGrade> findAllByUser_UserId(Long userId);
}
