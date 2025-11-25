package com.klp.user.infrastructure.repository;

import com.klp.common.exception.BusinessException;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public boolean existsByUsername(String username) {
        return userJpaRepository.existsByName(username);
    }

    @Override
    public User findById(Long userId) {
        User user = userJpaRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchByKeyword(String keyword, Pageable pageable) {
        return userJpaRepository.findAllByNameContaining(keyword, keyword, pageable);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userJpaRepository.findByName(username)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
