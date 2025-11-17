package com.klp.authservice.auth.domain.repository;

import com.klp.authservice.auth.domain.entity.BlackListToken;

public interface BlackListTokenRepository {

    BlackListToken save(BlackListToken token);

    boolean existsByToken(String token);
}
