package com.klp.hub.inventory.infrastructure.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.klp.hub.TestJpaConfig;
import com.klp.hub.inventory.domain.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@ActiveProfiles("test")
@Import({
    IdempotencyKeyRepositoryImpl.class,
    TestJpaConfig.class
})
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
class IdempotencyKeyRepositoryImplTest {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Test
    @DisplayName("같은 멱등키를 또 생성시도할때 false 를 반환한다")
    void throwDuplicateIdempotencyKey() {
        String idempotencyKeyA = "idempotencyKey";
        idempotencyKeyRepository.tryAcquireIdempotencyKey(idempotencyKeyA);
        String duplicateIdempotencyKey = "idempotencyKey";

        boolean result = idempotencyKeyRepository.tryAcquireIdempotencyKey(duplicateIdempotencyKey);

        assertFalse(result);
    }

    @Test
    @DisplayName("처음 멱등키를 생성을 시도한다면 true 를 반환한다")
    void createIdempotencyKey() {
        String idempotencyKey = "idempotencyKey";

        boolean result = idempotencyKeyRepository.tryAcquireIdempotencyKey(idempotencyKey);

        assertTrue(result);
    }

}
