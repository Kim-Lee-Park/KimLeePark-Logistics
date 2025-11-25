package com.klp.user.infrastructure.repository;

import com.klp.user.domain.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByName(String username);

    Page<User> findAllByNameContaining(String name, String slackId, Pageable pageable);

    Optional<User> findByName(String username);
}
