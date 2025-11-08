package com.klp.delivery.delivery.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.klp.common.IdempotencyStatus;
import com.klp.delivery.delivery.domain.IdempotencyKey;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(IdempotencyKeyRepositoryImpl.class)
public class IdempotencyRepositoryImplTest {


  @Autowired
  private IdempotencyKeyRepositoryImpl idempotencyKeyRepositoryImpl;


  @Test
  void repository가_null_아님을_검증() {
    Assertions.assertThat(idempotencyKeyRepositoryImpl).isNotNull();
  }


  @Test
  void 멱등키_등록_성공() {

    // given: 멱등키 등록 데이터 준비
    String key = "key";
    UUID orderId = UUID.randomUUID();

    // when: 멱등키 엔티티 생성
    IdempotencyKey idempotencyKey = IdempotencyKey.create(key, orderId);

    IdempotencyKey result = idempotencyKeyRepositoryImpl.save(idempotencyKey);

    // then: 생성 검증
    assertThat(result.getIdempotencyKey()).isNotNull();
    assertThat(result.getOrderId()).isEqualTo(orderId);
    assertThat(result.getStatus()).isEqualTo(IdempotencyStatus.PENDING);

  }


  @Test
  void 멱등키_조회_성공() {

    // given: 멱등키 등록 데이터 준비
    String key = "key";
    UUID orderId = UUID.randomUUID();

    // when: 멱등키 엔티티 생성 및 조회
    IdempotencyKey idempotencyKey = IdempotencyKey.create(key, orderId);

    idempotencyKeyRepositoryImpl.save(idempotencyKey);

    Optional<IdempotencyKey> result = idempotencyKeyRepositoryImpl.findByIdempotencyKey(key);

    // then: 생성 검증
    assertThat(result.get().getIdempotencyKey()).isNotNull();
    assertThat(result.get().getOrderId()).isEqualTo(orderId);
    assertThat(result.get().getStatus()).isEqualTo(IdempotencyStatus.PENDING);

  }


}
