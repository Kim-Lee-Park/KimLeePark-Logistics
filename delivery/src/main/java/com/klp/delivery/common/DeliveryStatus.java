package com.klp.delivery.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus {

  CREATED("배송 생성"),                   // 배송 생성
  AT_HUB_WAITING("허브 대기 중"),         // 첫 배송 시작 허브 출발 전
  HUB_TRANSIT("허브 간 이동 중"),          // 허브 이동 중
  AT_HUB_ARRIVED("허브 도착"),             // 허브 도착 (중간 허브 포함)
  ARRIVED_AT_FINAL_HUB("최종 허브 도착"),  // 배송지 인근 허브
  OUT_FOR_DELIVERY("배송 출발"),           // 고객지 이동 중
  DELIVERED("배송 완료");                 // 고객 수령 완료

  private final String description;

}
