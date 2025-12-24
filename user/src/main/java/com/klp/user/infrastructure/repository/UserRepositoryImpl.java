package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId);
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
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByName(username);
    }

    @Override
    public List<User> findDriversByHubId(UUID hubId) {
        return userJpaRepository.findByAffiliationIdAndRoleAndDeletedAtIsNull(
            hubId, UserRole.DRIVER
        );
    }

    @Override
    public List<User> findDriversByLogistics() {
        return userJpaRepository.findByAffiliationTypeAndRoleAndDeletedAtIsNull(
            AffiliationType.LOGISTICS, UserRole.DRIVER
        );
    }

    @Override
    public Optional<User> findDriverById(Long driverId) {
        return userJpaRepository.findByUserIdAndRoleAndDeletedAtIsNull(driverId, UserRole.DRIVER);
    }
}
