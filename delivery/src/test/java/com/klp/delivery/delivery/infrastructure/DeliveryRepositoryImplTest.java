package com.klp.delivery.delivery.infrastructure;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_SLACK_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_SENDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_VENDOR_DRIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.defaultDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.deliveryWithCustomHubId;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.DEFAULT_HUB_ID_UUID_SECOND;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_FIRST;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_SECOND;

import com.klp.delivery.common.enums.DeliveryStatus;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.entity.DeliveryItem;
import com.klp.delivery.delivery.infrastructure.repository.DeliveryItemRepositoryImpl;
import com.klp.delivery.delivery.infrastructure.repository.DeliveryJpaRepository;
import com.klp.delivery.delivery.infrastructure.repository.DeliveryRepositoryImpl;
import com.klp.delivery.global.config.AuditConfig;
import com.klp.delivery.global.config.QuerydslConfig;
import groovy.util.logging.Slf4j;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;

import static com.klp.delivery.delivery.fixture.OrderItemFixture.ORDER_ITEM_ID_THIRD;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@Import({DeliveryItemRepositoryImpl.class, DeliveryRepositoryImpl.class, AuditConfig.class, QuerydslConfig.class})
public class DeliveryRepositoryImplTest {


    @Autowired
    private DeliveryRepositoryImpl deliveryRepository;
    @Autowired
    private DeliveryJpaRepository deliveryJpaRepository;


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
        Delivery findResult = deliveryRepository.findByDeliveryId(saved.getDeliveryId());

        // then: 생성 검증
        assertThat(findResult)
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
        Delivery findResult = deliveryRepository.findByDeliveryId(saved.getDeliveryId());
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


    @Test
    void 주문id로_주문조회() {
        // given: 배송 생성 및 저장
        Delivery delivery1 = defaultDelivery();

        List<OrderItemCommand> items = List.of(
            new OrderItemCommand(ORDER_ITEM_ID_THIRD, DEFAULT_HUB_ID_UUID_SECOND));

        Delivery delivery2 = deliveryWithCustomHubId(items);

        // when: 배송 저장 (CascadeType.ALL로 인해 아이템도 함께 저장)
        Delivery saved = deliveryRepository.save(delivery1);
        deliveryRepository.save(delivery2);

        // when: 배송 저장 조회
        List<Delivery> findResult = deliveryRepository.findDeliveryByOrderId(saved.getOrderId());

        // then: 배송 저장으로 인해 배송아이템도 함께 저장되었는지 검증
        assertThat(findResult).hasSize(2);
        assertThat(findResult).isNotEmpty();

        Assertions.assertThat(findResult)
            .flatExtracting(Delivery::getDeliveryItems)
            .extracting(DeliveryItem::getOrderItemId)
            .containsExactlyInAnyOrder(ORDER_ITEM_ID_FIRST, ORDER_ITEM_ID_SECOND,
                ORDER_ITEM_ID_THIRD);

        Assertions.assertThat(findResult)
            .extracting(Delivery::getDepartureId)
            .containsExactlyInAnyOrder(DEFAULT_HUB_ID_UUID_FIRST, DEFAULT_HUB_ID_UUID_SECOND);
    }

    @Test
    void 배송_전체_조회_성공() {
        // given: 배송 생성 및 저장
        for (int i = 0; i < 22; i++) {
            deliveryJpaRepository.save(
                Delivery.create(
                    DEFAULT_VENDOR_DRIVER_ID,
                    UUID.randomUUID(),
                    DEFAULT_DEPARTURE_ID,
                    DEFAULT_ARRIVAL_ID,
                    DEFAULT_SENDER_ID,
                    DEFAULT_RECEIVER_ID,
                    DEFAULT_COMPANY_NAME,
                    DEFAULT_COMPANY_ADDRESS,
                    DEFAULT_RECEIVER_SLACK_ID,
                    List.of()
                )
            );
        }


        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());


        // when
        Page<Delivery> page = deliveryRepository.findDeliveryAll(pageable);

        // then
        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(10);
        assertThat(page.getTotalElements()).isEqualTo(22);
    }



}
