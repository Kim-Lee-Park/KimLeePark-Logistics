package com.klp.delivery.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryRouteStatus {

  CREATED("배송 생성"),                    // 배송 생성됨 (허브 도착 전)
  IN_HUB_TRANSIT("허브 간 이동 중"),        // 허브 이동 중
  AT_INTERMEDIATE_HUB("허브 도착"),        // 중간 허브 도착
  ARRIVED_AT_FINAL_HUB("최종 허브 도착"),   // 최종 허브 도착
  OUT_FOR_DELIVERY("배송 출발"),           // 고객에게 배송 중
  DELIVERED("배송 완료");                  // 배송완료

  private final String description;

}
