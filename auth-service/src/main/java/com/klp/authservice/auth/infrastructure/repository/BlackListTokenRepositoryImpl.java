package com.klp.authservice.auth.infrastructure.repository;

import com.klp.authservice.auth.domain.entity.BlackListToken;
import com.klp.authservice.auth.domain.repository.BlackListTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlackListTokenRepositoryImpl implements BlackListTokenRepository {

    private final BlackListTokenJpaRepository blackListTokenJpaRepository;

    @Override
    public BlackListToken save(BlackListToken token) {
        return blackListTokenJpaRepository.save(token);
    }

    @Override
    public boolean existsByToken(String token) {
        return blackListTokenJpaRepository.existsByToken(token);
    }
}
