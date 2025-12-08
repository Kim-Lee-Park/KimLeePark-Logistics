package com.klp.delivery.delivery.presentation.controller.docs;

import com.klp.delivery.delivery.presentation.dto.DeliveryRouteDetailResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

@Tag(name = "Delivery Route API", description = "배송 경로 관리 API")
public interface DeliveryRouteControllerDocs {

    @Operation(summary = "배송 경로 생성", description = "배송 ID로 배송 경로를 생성합니다.")
    ResponseEntity<DeliveryRouteResponse> appendDeliveryRoute(
        @Parameter(description = "배송 ID") UUID deliveryId
    );

    @Operation(summary = "배송 경로 조회", description = "배송 ID로 배송 경로 목록을 조회합니다.")
    ResponseEntity<List<DeliveryRouteDetailResponse>> findDeliveryRoutes(
        @Parameter(description = "배송 ID") UUID deliveryId
    );
}
