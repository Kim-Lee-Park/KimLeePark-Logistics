package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.UserGrade;
import com.klp.user.domain.repository.UserGradeRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserGradeRepositoryImpl implements UserGradeRepository {

    private final UserGradeJpaRepository userGradeJpaRepository;

    @Override
    public Optional<UserGrade> findFirstByUser(Long userId) {
        return userGradeJpaRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
    }

    @Override
    public UserGrade save(UserGrade userGrade) {
        return userGradeJpaRepository.save(userGrade);
    }
}
