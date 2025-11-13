package com.klp.delivery.delivery.repository;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.common.JpaAuditingConfig;
import com.klp.delivery.delivery.domain.entity.Delivery;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({DeliveryRepositoryImpl.class, JpaAuditingConfig.class})
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
        Delivery delivery = createDelivery();

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
        Delivery delivery = createDelivery();

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


}
