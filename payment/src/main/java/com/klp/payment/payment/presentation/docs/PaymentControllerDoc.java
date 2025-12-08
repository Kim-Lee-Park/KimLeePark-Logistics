package com.klp.payment.payment.presentation.docs;

import com.klp.payment.global.security.model.UserDetailsImpl;
import com.klp.payment.payment.presentation.dto.request.ApprovePaymentRequest;
import com.klp.payment.payment.presentation.dto.request.CancelPaymentRequest;
import com.klp.payment.payment.presentation.dto.request.FailPaymentRequest;
import com.klp.payment.payment.presentation.dto.request.PreparePaymentRequest;
import com.klp.payment.payment.presentation.dto.response.PaymentListResponse;
import com.klp.payment.payment.presentation.dto.response.PaymentResponse;
import com.klp.payment.payment.presentation.dto.response.PreparePaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Payment API", description = "결제 관리 API")
public interface PaymentControllerDoc {

    @Operation(summary = "결제 준비", description = "CUSTOMER 권한으로 결제를 준비합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "결제 준비 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<PreparePaymentResponse> preparePayment(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "결제 준비 요청 정보", required = true)
        @Valid @RequestBody PreparePaymentRequest request
    );

    @Operation(summary = "결제 승인", description = "CUSTOMER 권한으로 본인의 결제를 승인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 승인 성공"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<PaymentResponse> approvePayment(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "결제 ID", required = true)
        @PathVariable UUID paymentId,
        @Parameter(description = "결제 승인 요청 정보", required = true)
        @Valid @RequestBody ApprovePaymentRequest request
    );

    @Operation(summary = "결제 상세 조회", description = "결제 ID로 결제 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    ResponseEntity<PaymentResponse> getPaymentById(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "결제 ID", required = true)
        @PathVariable UUID paymentId
    );

    @Operation(summary = "결제 목록 조회", description = "권한에 따라 결제 목록을 조회합니다. MASTER: 전체, HUB: 본인 허브, CUSTOMER: 본인 결제")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<PaymentListResponse> getAllPayments(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "페이지네이션 정보")
        Pageable pageable
    );

    @Operation(summary = "주문별 결제 조회", description = "주문 ID로 관련된 결제 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    ResponseEntity<List<PaymentResponse>> getPaymentByOrderId(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId
    );

    @Operation(summary = "결제 취소", description = "CUSTOMER 권한으로 본인의 결제를 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음"),
        @ApiResponse(responseCode = "400", description = "취소 불가능한 상태")
    })
    ResponseEntity<PaymentResponse> cancelPayment(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "결제 ID", required = true)
        @PathVariable UUID paymentId,
        @Parameter(description = "결제 취소 요청 정보", required = true)
        @Valid @RequestBody CancelPaymentRequest request
    );

    @Operation(summary = "결제 실패 처리", description = "CUSTOMER 권한으로 결제 실패를 처리합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "결제 실패 처리 성공"),
        @ApiResponse(responseCode = "404", description = "결제를 찾을 수 없음")
    })
    ResponseEntity<PaymentResponse> failPayment(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "결제 ID", required = true)
        @PathVariable UUID paymentId,
        @Parameter(description = "결제 실패 요청 정보", required = true)
        @Valid @RequestBody FailPaymentRequest request
    );
}
