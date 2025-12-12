//package com.klp.delivery.delivery.infrastructure;
//
//
//import static com.klp.delivery.delivery.fixture.DeliveryFixture.defaultDelivery;
//import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
//import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_SECOND;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.klp.delivery.delivery.domain.entity.Delivery;
//import com.klp.delivery.delivery.domain.entity.DeliveryItem;
//import com.klp.delivery.delivery.infrastructure.repository.DeliveryItemRepositoryImpl;
//import com.klp.delivery.delivery.infrastructure.repository.DeliveryRepositoryImpl;
//import com.klp.delivery.global.config.AuditConfig;
//import com.klp.delivery.global.config.QuerydslConfig;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//
//@DataJpaTest
//@ActiveProfiles("test")
//@Import({DeliveryItemRepositoryImpl.class, DeliveryRepositoryImpl.class, AuditConfig.class, QuerydslConfig.class})
//public class DeliveryItemRepositoryImplTest {
//
//    @Autowired
//    private DeliveryItemRepositoryImpl deliveryItemRepository;
//
//    @Autowired
//    private DeliveryRepositoryImpl deliveryRepository;
//
//    @Test
//    void repository가_null_아님을_검증() {
//        // given & when: Repository 주입 확인
//        // then: Repository가 null이 아님
//        assertThat(deliveryItemRepository).isNotNull();
//        assertThat(deliveryRepository).isNotNull();
//    }
//
//    @Test
//    void 배송아이템_등록_성공() {
//        // given: 배송 생성 및 저장
//        Delivery delivery = defaultDelivery();
//        Delivery savedDelivery = deliveryRepository.save(delivery);
//        UUID deliveryId = savedDelivery.getDeliveryId();
//
//        UUID orderItemId1 = UUID.randomUUID();
//        UUID orderItemId2 = UUID.randomUUID();
//
//        DeliveryItem deliveryItem1 = DeliveryItem.create(savedDelivery, orderItemId1);
//        DeliveryItem deliveryItem2 = DeliveryItem.create(savedDelivery, orderItemId2);
//
//        List<DeliveryItem> deliveryItems = Arrays.asList(deliveryItem1, deliveryItem2);
//
//        // when: 배송 아이템 복수 등록
//        List<DeliveryItem> result = deliveryItemRepository.saveAll(deliveryItems);
//
//        // then: 저장된 모든 DeliveryItem의 deliveryId가 같고 주문 아이템들 검증
//        assertThat(result).isNotNull().hasSize(2);
//
//        assertThat(result).extracting(item -> item.getDelivery().getDeliveryId())
//            .containsOnly(deliveryId);
//
//        assertThat(result).extracting(DeliveryItem::getOrderItemId)
//            .containsExactlyInAnyOrder(orderItemId1, orderItemId2);
//    }
//
//    @Test
//    void 배송ID로_배송아이템_조회_성공() {
//        // given: 배송 생성 및 저장
//        Delivery delivery = defaultDelivery();
//
//        // when: 배송 저장 (CascadeType.ALL로 인해 아이템도 함께 저장)
//        Delivery saved = deliveryRepository.save(delivery);
//
//        // when: 배송 ID로 배송 아이템 조회
//        List<DeliveryItem> result = deliveryItemRepository.findAllByDeliveryId(
//            saved.getDeliveryId());
//
//        // then: 저장된 모든 DeliveryItem의 deliveryId가 같고 주문 아이템들 검증
//        assertThat(result).isNotNull().hasSize(2);
//
//        assertThat(result).extracting(item -> item.getDelivery().getDeliveryId())
//            .containsOnly(saved.getDeliveryId());
//
//        assertThat(result).extracting(DeliveryItem::getOrderItemId)
//            .containsExactlyInAnyOrder(ORDER_ITEM_ID_FIRST, ORDER_ITEM_ID_SECOND);
//    }
//
//    @Test
//    void 배송과_함께_배송아이템_저장_성공() {
//        // given: 배송 생성 및 저장
//        Delivery delivery = defaultDelivery();
//        Delivery savedDelivery = deliveryRepository.save(delivery);
//
//        UUID orderItemId1 = UUID.randomUUID();
//        DeliveryItem deliveryItem1 = DeliveryItem.create(savedDelivery, orderItemId1);
//
//        UUID orderItemId2 = UUID.randomUUID();
//        DeliveryItem deliveryItem2 = DeliveryItem.create(savedDelivery, orderItemId2);
//
//        // when: 배송 아이템 저장
//        List<DeliveryItem> result = deliveryItemRepository.saveAll(
//            List.of(deliveryItem1, deliveryItem2));
//
//        // then: 배송과 연관관계가 정상적으로 저장되었는지 검증
//        assertThat(result).isNotNull().hasSize(2);
//        assertThat(result.get(0).getDelivery()).isNotNull();
//        assertThat(result.get(0).getDelivery().getDeliveryId())
//            .isEqualTo(savedDelivery.getDeliveryId());
//        assertThat(result.get(0).getOrderItemId()).isEqualTo(orderItemId1);
//    }
//
//}
