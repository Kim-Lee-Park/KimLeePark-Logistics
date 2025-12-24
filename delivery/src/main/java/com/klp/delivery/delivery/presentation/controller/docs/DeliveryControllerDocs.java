package com.klp.delivery.delivery.presentation.controller.docs;

import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Delivery API", description = "배송 관리 API")
public interface DeliveryControllerDocs {

    @Operation(summary = "배송 생성", description = "주문 정보로 배송을 생성합니다.")
    ResponseEntity<DeliveryResponse> createDelivery(@RequestBody DeliveryCreateRequest request);

    @Operation(summary = "배송 단건 조회", description = "배송 ID로 상세 정보를 조회합니다.")
    ResponseEntity<DeliveryDetailResponse> getDelivery(@Parameter(description = "배송 ID") UUID deliveryId);

    @Operation(summary = "주문별 배송 목록 조회", description = "주문 ID로 해당 주문의 모든 배송을 조회합니다.")
    ResponseEntity<List<DeliveryDetailResponse>> getDeliveriesByOrderId(
        @Parameter(description = "주문 ID") UUID orderId
    );

    @Operation(summary = "배송 목록 조회", description = "페이지네이션으로 배송 목록을 조회합니다.")
    ResponseEntity<Page<DeliveryDetailResponse>> getAllDeliveries(@ParameterObject Pageable pageable);

    @Operation(summary = "배송 상태 업데이트", description = "배송 상태(기사/벤더)를 수정합니다.")
    ResponseEntity<Void> updateDeliveryStatus(
        @Parameter(description = "배송 ID") UUID deliveryId,
        @RequestBody DeliveryUpdateRequest request
    );

    @Operation(summary = "배송 삭제", description = "배송을 삭제합니다.")
    ResponseEntity<Void> deleteDelivery(
        @Parameter(description = "배송 ID") UUID deliveryId,
        @Parameter(description = "삭제 요청 사용자 ID") @RequestHeader(value = "X-User-Id", required = false, defaultValue = "0") Long deletedBy
    );
}
