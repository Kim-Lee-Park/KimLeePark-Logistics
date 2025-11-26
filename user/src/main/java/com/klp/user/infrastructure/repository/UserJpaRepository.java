package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByName(String username);

    Page<User> findAllByNameContaining(String name, String slackId, Pageable pageable);

    Optional<User> findByName(String username);

    List<User> findByAffiliationIdAndRoleAndDeletedAtIsNull(UUID affiliationId, UserRole role, Pageable pageable);

    List<User> findByAffiliationTypeAndRoleAndDeletedAtIsNull(AffiliationType affiliationType, UserRole role, Pageable pageable);

    Optional<User> findByUserIdAndRoleAndDeletedAtIsNull(Long userId, UserRole role);
}
