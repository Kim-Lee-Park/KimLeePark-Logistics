package com.klp.delivery.delivery.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.domain.entity.IdempotencyKey;
import com.klp.delivery.delivery.domain.repository.IdempotencyKeyRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerIdempotencyKey(IdempotencyCommand command) {

        Optional<IdempotencyKey> key = idempotencyKeyRepository.findByIdempotencyKey(
            command.idempotencyKey());

        if (key.isPresent()) {
            throw new BusinessException(DeliveryErrorCode.DUPLICATE_IDEMPOTENCY_KEY);
        }

        idempotencyKeyRepository.save(
            IdempotencyKey.create(command.idempotencyKey(), command.orderId(), command.status()));
    }


    public void updateIdempotencyStatus(IdempotencyCommand command) {

        idempotencyKeyRepository.findByIdempotencyKey(command.idempotencyKey())
            .ifPresent(k -> k.updateStatus(command.status()));

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteIdempotencyKey(String idempotencyKey) {
        log.info("멱등키 삭제: idempotencyKey={}", idempotencyKey);
        idempotencyKeyRepository.deleteByIdempotencyKey(idempotencyKey);
    }
}
