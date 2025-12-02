package com.klp.user.domain.repository;

import com.klp.user.domain.entity.UserGrade;
import java.util.Optional;

public interface UserGradeRepository {

    Optional<UserGrade> findFirstByUser(Long userId);

    UserGrade save(UserGrade userGrade);
}
