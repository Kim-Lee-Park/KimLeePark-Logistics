package com.klp.delivery.delivery.application;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.entity.IdempotencyKey;
import com.klp.delivery.delivery.domain.repository.IdempotencyKeyRepository;
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
    // given: 이미 존재하는 멱등키
    String idempotencyKey = "멱등키123";
    UUID orderId = UUID.randomUUID();
    IdempotencyKey existingKey = IdempotencyKey.create(idempotencyKey, orderId, IdempotencyStatus.PENDING);
    IdempotencyCommand command = new IdempotencyCommand(
        idempotencyKey, 
        orderId, 
        IdempotencyStatus.PENDING
    );
    
    when(idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey))
        .thenReturn(Optional.of(existingKey));

    // when & then: 중복 멱등키로 인한 예외 발생 검증
    Assertions.assertThrows(
        com.klp.common.exception.BusinessException.class,
        () -> idempotencyKeyService.registerIdempotencyKey(command)
    );
    
    // then: 멱등키 조회는 호출되었지만 저장은 호출되지 않음
    verify(idempotencyKeyRepository, times(1)).findByIdempotencyKey(idempotencyKey);
    verify(idempotencyKeyRepository, never()).save(any(IdempotencyKey.class));
  }


  @Test
  void 멱등키_생성_성공() {

    // given 멱등키 생성
    String idemKey = "key";
    UUID orderId = UUID.randomUUID();
    IdempotencyCommand command = new IdempotencyCommand(idemKey, orderId, IdempotencyStatus.PENDING);

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

    IdempotencyCommand command = new IdempotencyCommand(idemKey, orderId, IdempotencyStatus.COMPLETED);

    IdempotencyKey key = IdempotencyKey.create(command.idempotencyKey(), command.orderId(), command.status());

    when(idempotencyKeyRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.of(key));

    // when: 멱등키 상태 변경
    idempotencyKeyService.updateIdempotencyStatus(command);

    // then: 멱등키 조회 및 상태 변경 검증
    verify(idempotencyKeyRepository, times(1)).findByIdempotencyKey(idemKey);
    assertThat(key.getStatus()).isEqualTo(IdempotencyStatus.COMPLETED);
  }


}
