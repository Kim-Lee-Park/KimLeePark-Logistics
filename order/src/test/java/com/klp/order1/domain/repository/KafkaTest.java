package com.klp.order1.domain.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.klp.order.infrastructure.event.event.DeliveryCreatedEvent;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@ExtendWith(MockitoExtension.class)
public class KafkaTest {

    @Test
    void AEvent_JSON에_필드가_더_많아도_B모듈_AEvent로_역직렬화된다() {
        // given — A 모듈에서 발행하는 JSON
        String json = """
{
  "@type": "DeliveryCreatedEvent",
  "orderId": "11111111-1111-1111-1111-111111111111",
  "userId": 1001,
  "supplierId": "22222222-2222-2222-2222-222222222222",
  "userCouponId": "33333333-3333-3333-3333-333333333333",
  "email": "user@test.com",
  "username": "kim",
  "username2": "kim",
  "comment": "문 앞에 놔주세요",
  "comment2": "문 앞에 놔주세요",
  "originalPrice": 50000,
  "couponDiscountPrice": 5000,
  "gradeDiscountPrice": 2000,
  "finalOrderPrice": 43000,
  "addressId": "44444444-4444-4444-4444-444444444444",
  "userAddressHubId": "55555555-5555-5555-5555-555555555555",
  "address": "서울특별시 강남구 테헤란로 123",
  "deliveryLatitude": 37.4979,
  "deliveryLongitude": 127.0276,
  "products": [
    {
      "orderItemId": "66666666-6666-6666-6666-666666666666",
      "productId": "77777777-7777-7777-7777-777777777777",
      "productName": "상품A",
      "hubId": "88888888-8888-8888-8888-888888888888",
      "quantity": 2,
      "unitPrice": 20000,
      "totalPrice": 40000,
      "deliveryId": "99999999-9999-9999-9999-999999999999"
    }
  ],
  "inventoryIdempotencyKey": "inventory-idem-001",
  "deliveryIdempotencyKey": "delivery-idem-001",
  "createdAt": "2025-01-01T12:00:00",
  "occurredAt": "2025-01-01T12:00:01"
}
    """;

        // ObjectMapper 설정 (알려지지 않은 속성 무시)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // JsonDeserializer 설정 (실제 KafkaConfig와 동일하게)
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(objectMapper);
        deserializer.configure(Map.of(
            JsonDeserializer.TRUSTED_PACKAGES, "*",
            JsonDeserializer.USE_TYPE_INFO_HEADERS, true,
            JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class,
            JsonDeserializer.TYPE_MAPPINGS,
            "DeliveryCreatedEvent:com.klp.order.infrastructure.event.event.DeliveryCreatedEvent" // B 모듈 AEvent로 매핑
        ), false);

        // when - Kafka 헤더를 시뮬레이션 (JsonSerializer가 자동으로 추가하는 __TypeId__ 헤더)
        org.apache.kafka.common.header.Headers headers = new org.apache.kafka.common.header.internals.RecordHeaders();
        headers.add("__TypeId__", "DeliveryCreatedEvent".getBytes());
        
        Object result = deserializer.deserialize("topic-name", headers, json.getBytes());

        // then
        assertThat(result).isInstanceOf(DeliveryCreatedEvent.class);

        DeliveryCreatedEvent event = (DeliveryCreatedEvent) result;
        assertThat(event.orderId()).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        assertThat(event.products().get(0).deliveryId()).isEqualTo(UUID.fromString("33333333-3333-3333-3333-333333333333"));

        // JSON에는 있었지만 BEvent에는 없는 필드는 자동 무시됨 → 오류 없음
    }

}
