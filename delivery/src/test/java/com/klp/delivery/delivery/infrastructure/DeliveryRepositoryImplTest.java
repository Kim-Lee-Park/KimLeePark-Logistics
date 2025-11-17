package com.klp.delivery.delivery.infrastructure;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.defaultDelivery;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_SECOND;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.infrastructure.repository.DeliveryRepositoryImpl;
import com.klp.delivery.global.config.AuditConfig;
import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@Import({DeliveryRepositoryImpl.class, AuditConfig.class})
public class DeliveryRepositoryImplTest {


    @Autowired
    private DeliveryRepositoryImpl deliveryRepository;


    @Test
    void repository가_null_아님을_검증() {
        Assertions.assertThat(deliveryRepository).isNotNull();
    }


    @Test
    void 배송_등록_성공() {

        // given: 배송 등록 데이터 준비
        Delivery delivery = defaultDelivery();

        // when: 배송 엔티티 생성
        Delivery result = deliveryRepository.save(delivery);

        // then: 생성 검증
        assertThat(result.getDeliveryId()).isNotNull();
        assertThat(delivery.getOrderId()).isEqualTo(DEFAULT_ORDER_ID);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);

    }


    @Test
    void 배송ID로_배송_조회_성공() {

        // given: 배송 등록 데이터 준비
        Delivery delivery = defaultDelivery();

        // when: 배송 엔티티 생성
        Delivery saved = deliveryRepository.save(delivery);
        Optional<Delivery> findResult = deliveryRepository.findByDeliveryId(saved.getDeliveryId());

        // then: 생성 검증
        assertThat(findResult)
            .isPresent()
            .get()
            .extracting(Delivery::getStatus)
            .isEqualTo(DeliveryStatus.CREATED);

    }


    @Test
    void 배송과_함께_배송아이템_저장_성공_연관관계확인() {
        // given: 배송 생성 및 저장
        Delivery delivery = defaultDelivery();

        // when: 배송 저장 (CascadeType.ALL로 인해 아이템도 함께 저장)
        Delivery saved = deliveryRepository.save(delivery);

        // when: 배송 저장 조회
        Delivery findResult = deliveryRepository.findByDeliveryId(saved.getDeliveryId()).orElseThrow();
        List<DeliveryItem> items = findResult.getDeliveryItems();


        // then: 배송 저장으로 인해 배송아이템도 함께 저장되었는지 검증
        Assertions.assertThat(items.get(0).getDelivery())
            .isNotNull()
            .isEqualTo(findResult);

        Assertions.assertThat(items.get(0).getDelivery().getDeliveryId())
            .isEqualTo(findResult.getDeliveryId());

        Assertions.assertThat(items)
            .extracting(DeliveryItem::getOrderItemId)
            .containsExactlyInAnyOrder(ORDER_ITEM_ID_FIRST, ORDER_ITEM_ID_SECOND);
    }


}
