package com.klp.delivery.delivery.application;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.delivery.common.IdempotencyStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.IdempotencyKey;
import com.klp.delivery.delivery.domain.IdempotencyKeyRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class IdempotencyServiceTest extends MockTest {

  @InjectMocks
  IdempotencyKeyService idempotencyKeyService;

  @Mock
  IdempotencyKeyRepository idempotencyKeyRepository;


  @Test
  void 동일_멱등키로_배송_요청시_배송_생성_실패() {

    // given 멱등키 생성
    String idemKey = "key";
    UUID orderId = UUID.randomUUID();
    IdempotencyKey key = IdempotencyKey.create(idemKey, orderId);
    IdempotencyCommand command = new IdempotencyCommand(idemKey, orderId);
    Mockito.doReturn(Optional.of(key)).when(idempotencyKeyRepository).findByIdempotencyKey(idemKey);

    // when 기존 멱등키 확인
    Exception ex = Assertions.assertThrows(RuntimeException.class,
        () -> idempotencyKeyService.registerIdempotencyKey(command));
    assertThat(ex.getMessage()).contains("이미 처리된 요청입니다");

  }


  @Test
  void 멱등키_생성_성공() {

    // given 멱등키 생성
    String idemKey = "key";
    UUID orderId = UUID.randomUUID();
    IdempotencyCommand command = new IdempotencyCommand(idemKey, orderId);

    Mockito.doReturn(Optional.empty()).when(idempotencyKeyRepository).findByIdempotencyKey(idemKey);

    // when: 멱등키 생성 요청
    idempotencyKeyService.registerIdempotencyKey(command);

    // then: 멱등키 저장 호출 검증
    verify(idempotencyKeyRepository, times(1)).save(any(IdempotencyKey.class));

  }


  @Test
  void 멱등키_상태변경_성공() {

    // given 멱등키 생성
    String idemKey = "key";
    UUID orderId = UUID.randomUUID();
    IdempotencyKey key = IdempotencyKey.create(idemKey, orderId);
    IdempotencyCommand command = new IdempotencyCommand(idemKey, orderId);
    when(idempotencyKeyRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.of(key));

    // when: 멱등키 생성 요청
    idempotencyKeyService.updateIdempotencyStatus(command);

    // then: 멱등키 조회 확인 밑 멱등키 상태 검증
    verify(idempotencyKeyRepository, times(1)).findByIdempotencyKey(command.idempotencyKey());
    assertThat(key.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);

  }


}
