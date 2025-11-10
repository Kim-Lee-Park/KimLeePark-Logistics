package com.klp.delivery.delivery.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.domain.IdempotencyKey;
import com.klp.delivery.delivery.domain.IdempotencyKeyRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {

  private final IdempotencyKeyRepository idempotencyKeyRepository;
  
  public void registerIdempotencyKey(IdempotencyCommand command) {

    Optional<IdempotencyKey> key = idempotencyKeyRepository.findByIdempotencyKey(
        command.idempotencyKey());

    if (key.isPresent()) {
      throw new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
    }

    idempotencyKeyRepository.save(
        IdempotencyKey.create(command.idempotencyKey(), command.orderId()));
  }


  public void updateIdempotencyStatus(IdempotencyCommand command) {

    idempotencyKeyRepository.findByIdempotencyKey(command.idempotencyKey())
        .ifPresent(k -> k.updateStatus(IdempotencyStatus.COMPLETED));


  }
}
