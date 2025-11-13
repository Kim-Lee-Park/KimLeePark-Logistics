package com.klp.delivery.delivery.domain;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryWithAddress;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryWithReceiverName;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDeliveryWithSlackId;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.fixture.DeliveryFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;


public class DeliveryTest extends MockTest {

    @Test
    void 배송_생성시_배송상태는_CREATED_설정_검증() {
        // given: 배송 등록 데이터 준비
        Delivery delivery = createDelivery();

        // then: 배송 상태값 검증
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
    }

    @Test
    void 배송_생성시_배송담당자가없으면_예외발생() {
        // given: 배송 담당자가 null인 데이터

        // when & then: 예외 발생 검증
        assertThatThrownBy(() -> DeliveryFixture.createDeliveryWithVendorDriverId(null))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode()).isEqualTo(
                    DeliveryErrorCode.INVALID_DELIVERY_DATA);
            });
    }

    @ParameterizedTest(name = "주소가 \"{0}\" 인 경우 예외 발생")
    @NullAndEmptySource               // null, "" 자동 포함
    @ValueSource(strings = {"   "})
        // 공백만 있는 문자열 추가
    void 배송_생성시_주소가_유효하지않으면_예외발생(String invalidAddress) {

        // when & then
        assertThatThrownBy(() -> createDeliveryWithAddress(invalidAddress))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode())
                    .isEqualTo(DeliveryErrorCode.INVALID_DELIVERY_DATA);
            });
    }

    @ParameterizedTest(name = "수령인 이름이 \"{0}\" 인 경우 예외 발생")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void 배송_생성시_수령인이름이_유효하지않으면_예외발생(String invalidName) {
        // when & then
        assertThatThrownBy(() -> createDeliveryWithReceiverName(invalidName))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode())
                    .isEqualTo(DeliveryErrorCode.INVALID_DELIVERY_DATA);
            });
    }

    @ParameterizedTest(name = "수령인 슬랙ID가 \"{0}\" 인 경우 예외 발생")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void 배송_생성시_슬랙ID가_유효하지않으면_예외발생(String invalidSlackId) {
        // when & then
        assertThatThrownBy(() -> createDeliveryWithSlackId(invalidSlackId))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException businessException = (BusinessException) exception;
                assertThat(businessException.getErrorCode())
                    .isEqualTo(DeliveryErrorCode.INVALID_DELIVERY_DATA);
            });
    }

    @ParameterizedTest(name = "상태가 {0} → {1} 로 변경될 수 있다")
    @CsvSource({
        "IN_HUB_TRANSIT, ARRIVED_AT_FINAL_HUB",   // 허브 이동 중 → 최종 허브 도착
        "OUT_FOR_DELIVERY, DELIVERED"          // 배송 출발 → 배송 완료
    })
    void 배송상태_정상변경_검증(DeliveryStatus from, DeliveryStatus to) {
        // given
        Delivery delivery = createDelivery();

        // when
        delivery.updateStatus(from);
        delivery.updateStatus(to);

        // then
        assertThat(delivery.getStatus()).isEqualTo(to);
    }
}
