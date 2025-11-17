package com.klp.delivery.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IdempotencyStatus {

  PENDING("요청"),
  COMPLETED("처리 완료"),
  FAILED("처리 실패");

  private final String description;

}
