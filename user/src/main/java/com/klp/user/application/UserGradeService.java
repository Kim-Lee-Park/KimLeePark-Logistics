package com.klp.user.application;

import com.klp.global.exception.BusinessException;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.entity.UserGrade;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserGradeRepository;
import com.klp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserGradeService {

    private final UserGradeRepository userGradeRepository;
    private final UserRepository userRepository;

    /**
     * 현재 등급명을 조회
     */
    @Transactional(readOnly = true)
    public String getCurrentGradeName(Long userId) {
        return userGradeRepository.findFirstByUser(userId)
            .map(UserGrade::getGradeName)
            .orElse(null);
    }

    /**
     * 현재 등급 엔티티를 조회
     */
    @Transactional(readOnly = true)
    public UserGrade getCurrentUserGrade(Long userId) {
        return userGradeRepository.findFirstByUser(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_GRADE_NOT_FOUND));
    }

    /**
     * 등급 생성
     */
    public UserGrade createUserGrade(User user, String gradeName) {
        UserGrade userGrade = UserGrade.create(user, gradeName);
        return userGradeRepository.save(userGrade);
    }

    /**
     * 등급 변경 (새로운 이력 생성)
     */
    public UserGrade updateUserGrade(Long userId, String gradeName) {
        User user = findNotDeletedUser(userId);
        UserGrade newGrade = UserGrade.create(user, gradeName);
        return userGradeRepository.save(newGrade);
    }

    private User findNotDeletedUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return user;
    }
}
