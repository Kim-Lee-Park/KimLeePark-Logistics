package com.klp.delivery.delivery.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.domain.entity.IdempotencyKey;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class IdempotencyKeyTest extends MockTest {

  @Test
  void 멱등키_생성시_멱등키상태는_PENDING_설정_검증() {

    // given: 멱등키 등록 데이터 준비
    String key = "key";
    UUID orderId = UUID.randomUUID();

    // when: 멱등키 생성
    IdempotencyKey idempotencyKey = IdempotencyKey.create(key, orderId, IdempotencyStatus.PENDING);

    // then: 멱등키 상태값 검증
    assertThat(idempotencyKey.getStatus()).isEqualTo(IdempotencyStatus.PENDING);
  }


  @Test
    void 멱등키_상태변경_검증(){

      // given: 멱등키 등록 데이터 준비
      String key = "key";
      UUID orderId = UUID.randomUUID();

      // when: 멱등키 생성
      IdempotencyKey idempotencyKey = IdempotencyKey.create(key, orderId, IdempotencyStatus.COMPLETED);

      idempotencyKey.updateStatus(IdempotencyStatus.COMPLETED);

      // then: 멱등키 상태 검증
      assertThat(idempotencyKey.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);

  }

}
