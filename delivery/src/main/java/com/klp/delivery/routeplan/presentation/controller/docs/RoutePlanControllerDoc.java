package com.klp.delivery.routeplan.presentation.controller.docs;

import com.klp.delivery.common.entity.UserDetailsImpl;
import com.klp.delivery.routeplan.presentation.dto.request.CreateRoutePlanRequest;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanItemDetailResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Route Plan API", description = "경로 계획 관리 API")
public interface RoutePlanControllerDoc {

    @Operation(summary = "경로 계획 생성", description = "MASTER 권한으로 경로 계획을 생성합니다.")
    ResponseEntity<CreateRoutePlanResponse> createRoutePlan(
        @Valid @RequestBody CreateRoutePlanRequest request
    );

    @Operation(
        summary = "출발/도착 허브로 경로 계획 조회",
        description = "출발 허브 ID와 도착 허브 ID로 경로 계획 상세를 조회합니다."
    )
    ResponseEntity<GetRoutePlanDetailResponse> getRoutePlanByDepartureAndArrival(
        @Parameter(description = "출발 허브 ID", required = true)
        @PathVariable UUID departureId,

        @Parameter(description = "도착 허브 ID", required = true)
        @PathVariable UUID arrivalId
    );

    @Operation(
        summary = "경로 계획 ID로 조회",
        description = "경로 계획 ID로 경로 계획 상세를 조회합니다."
    )
    ResponseEntity<GetRoutePlanDetailResponse> getRoutePlan(
        @Parameter(description = "경로 계획 ID", required = true)
        @PathVariable UUID routePlanId
    );

    @Operation(
        summary = "경로 계획 목록 조회",
        description = "출발/도착 허브 ID로 필터링하여 경로 계획 목록을 페이지네이션으로 조회합니다."
    )
    ResponseEntity<GetRoutePlanListResponse> getRoutePlans(
        @Parameter(description = "출발 허브 ID", required = false)
        @RequestParam(required = false) UUID depId,

        @Parameter(description = "도착 허브 ID", required = false)
        @RequestParam(required = false) UUID arrId,

        @ParameterObject Pageable pageable
    );

    @Operation(summary = "경로 계획 삭제", description = "MASTER 권한으로 경로 계획을 삭제합니다.")
    ResponseEntity<Void> deleteRoutePlan(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @Parameter(description = "경로 계획 ID", required = true)
        @PathVariable UUID routePlanId
    );

    @Operation(
        summary = "허브와 관련된 모든 경로 계획 삭제",
        description = "특정 허브 ID와 관련된 경로 계획들을 삭제 대상으로 마킹합니다."
    )
    ResponseEntity<Void> deleteRoutePlansByHubId(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @Parameter(description = "허브 ID", required = true)
        @PathVariable UUID hubId
    );

    @Operation(
        summary = "경로 계획 구간 정보 조회",
        description = "경로 계획 ID와 구간 아이템 ID로 경로 계획의 특정 구간 상세 정보를 조회합니다."
    )
    ResponseEntity<GetRoutePlanItemDetailResponse> getRoutePlanItem(
        @Parameter(description = "경로 계획 ID", required = true)
        @PathVariable UUID planId,

        @Parameter(description = "경로 계획 아이템 ID", required = true)
        @PathVariable UUID planItemId
    );
}
