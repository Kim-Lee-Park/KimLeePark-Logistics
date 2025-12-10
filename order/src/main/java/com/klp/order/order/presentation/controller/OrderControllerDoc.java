package com.klp.order.order.presentation.controller;

import com.klp.order.common.PageResponse;
import com.klp.order.order.presentation.dto.order.request.cancel.CancelOrderRequest;
import com.klp.order.order.presentation.dto.order.request.create.CreateOrderRequest;
import com.klp.order.order.presentation.dto.order.request.update.ChangeOrderStatusRequest;
import com.klp.order.order.presentation.dto.order.request.update.UpdateOrderRequest;
import com.klp.order.order.presentation.dto.order.response.cancel.CancelOrderResponse;
import com.klp.order.order.presentation.dto.order.response.create.CreateOrderResponse;
import com.klp.order.order.presentation.dto.order.response.delete.DeleteOrderResponse;
import com.klp.order.order.presentation.dto.order.response.get.GetOneOrderResponse;
import com.klp.order.order.presentation.dto.order.response.get.GetOrderProgressResponse;
import com.klp.order.order.presentation.dto.order.response.get.GetOrdersResponse;
import com.klp.order.order.presentation.dto.order.response.update.ChangeOrderStatusResponse;
import com.klp.order.order.presentation.dto.order.response.update.UpdateOrderResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Order API", description = "주문 관리 API")
public interface OrderControllerDoc {

    @Operation(
        summary = "주문 생성",
        description = "새로운 주문을 생성합니다. 주문 아이템은 최소 1개 이상이어야 합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "주문이 성공적으로 생성됨",
            content = @Content(schema = @Schema(implementation = CreateOrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 값 누락, 유효하지 않은 값 등)"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    ResponseEntity<CreateOrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    );

    @Operation(
        summary = "주문 목록 조회",
        description = "조건에 따라 주문 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        )
    })
    ResponseEntity<PageResponse<GetOrdersResponse>> getOrders(
        @Parameter(description = "공급업체 ID")
        @RequestParam(required = false) UUID supplierId,

        @Parameter(description = "고객 ID")
        @RequestParam(required = false) Long customerId,

        @Parameter(description = "생성자 ID")
        @RequestParam(required = false) Long createdBy,

        @Parameter(description = "조회 시작 날짜 (yyyy-MM-dd)", example = "2024-01-01")
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,

        @Parameter(description = "조회 종료 날짜 (yyyy-MM-dd)", example = "2024-12-31")
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,

        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "주문 상세 조회",
        description = "주문 ID로 특정 주문의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 조회 성공",
            content = @Content(schema = @Schema(implementation = GetOneOrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    ResponseEntity<GetOneOrderResponse> getOrder(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId
    );

    @Operation(
        summary = "주문 수정",
        description = "기존 주문의 내용을 수정합니다. 배송이 할당되거나 완료된 주문은 수정할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 수정 성공",
            content = @Content(schema = @Schema(implementation = UpdateOrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "수정할 수 없는 주문 상태"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    ResponseEntity<UpdateOrderResponse> updateOrder(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId,

        @Valid @RequestBody UpdateOrderRequest request
    );

    @Operation(
        summary = "주문 삭제 (소프트 삭제)",
        description = "주문을 논리적으로 삭제합니다. 실제 데이터는 삭제되지 않고 삭제 플래그만 설정됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 삭제 성공",
            content = @Content(schema = @Schema(implementation = DeleteOrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    ResponseEntity<DeleteOrderResponse> deleteOrder(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId,

        @Parameter(description = "삭제자 ID", required = true)
        @RequestHeader("X-User-Id") Long deletedBy
    );

    @Operation(
        summary = "주문 취소",
        description = "주문을 취소합니다. 배송 중이거나 완료된 주문은 취소할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 취소 성공",
            content = @Content(schema = @Schema(implementation = CancelOrderResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "취소할 수 없는 주문 상태"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    ResponseEntity<CancelOrderResponse> cancelOrder(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId,

        @Parameter(description = "취소자 ID", required = true)
        @RequestHeader("X-User-Id") Long cancelledBy,

        @Valid @RequestBody CancelOrderRequest request
    );

    @Operation(
        summary = "주문 상태 변경",
        description = "주문의 상태를 변경합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "주문 상태 변경 성공",
            content = @Content(schema = @Schema(implementation = ChangeOrderStatusResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "변경할 수 없는 주문 상태"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "주문을 찾을 수 없음"
        )
    })
    ResponseEntity<ChangeOrderStatusResponse> changeOrderStatus(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId,

        @Valid @RequestBody ChangeOrderStatusRequest request
    );

    @Operation(
        summary = "허브별 진행 중인 주문 확인",
        description = "특정 허브에 진행 중인 주문이 있는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = GetOrderProgressResponse.class))
        )
    })
    ResponseEntity<GetOrderProgressResponse> getOrderStatus(
        @Parameter(description = "허브 ID", required = true)
        @RequestParam UUID hubId
    );

    @Hidden
    ResponseEntity<Void> cache(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );
}