package com.klp.authservice.auth.infrastructure.repository;

import com.klp.authservice.auth.domain.entity.BlackListToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenJpaRepository extends JpaRepository<BlackListToken, UUID> {

    boolean existsByToken(String token);
}
