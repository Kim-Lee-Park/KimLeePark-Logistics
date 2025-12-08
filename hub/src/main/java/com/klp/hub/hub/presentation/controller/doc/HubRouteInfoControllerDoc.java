package com.klp.hub.hub.presentation.controller.doc;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.RegisterHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.UpdateHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
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

@Tag(name = "Hub Route Info API", description = "허브 간 이동 정보 관리 API")
public interface HubRouteInfoControllerDoc {

    @Operation(summary = "허브 간 이동 정보 생성", description = "MASTER 권한으로 허브 간 이동 정보를 생성합니다.")
    ResponseEntity<RegisterHubRouteInfoResponse> registerHubRouteInfo(
        @Valid @RequestBody RegisterHubRouteInfoRequest request
    );

    @Operation(summary = "허브 간 이동 정보 단일 조회", description = "허브 간 이동 정보 ID로 상세 조회합니다.")
    ResponseEntity<GetHubRouteInfoDetailResponse> getHubRouteInfoDetail(
        @Parameter(description = "허브 간 이동 정보 ID", required = true)
        @PathVariable UUID routeInfoId
    );

    @Operation(
        summary = "허브 간 이동 정보 목록 조회",
        description = "출발 허브 ID, 도착 허브 ID로 필터링하여 허브 간 이동 정보를 조회합니다."
    )
    ResponseEntity<GetHubRouteInfoListResponse> getHubRouteInfos(
        @Parameter(description = "출발 허브 ID", required = false)
        @RequestParam(required = false) UUID departureId,

        @Parameter(description = "도착 허브 ID", required = false)
        @RequestParam(required = false) UUID arrivalId,

        @ParameterObject Pageable pageable
    );

    @Operation(summary = "허브 간 이동 정보 수정", description = "MASTER 권한으로 허브 간 이동 정보를 수정합니다.")
    ResponseEntity<UpdatedHubRouteInfoResponse> updateHubRouteInfo(
        @Parameter(description = "허브 간 이동 정보 ID", required = true)
        @PathVariable UUID routeInfoId,

        @RequestBody UpdateHubRouteInfoRequest request
    );

    @Operation(summary = "허브 간 이동 정보 삭제", description = "MASTER 권한으로 허브 간 이동 정보를 삭제합니다.")
    ResponseEntity<Void> deleteHub(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @Parameter(description = "허브 간 이동 정보 ID", required = true)
        @PathVariable UUID routeInfoId
    );

    @Operation(
        summary = "모든 허브 간 이동 정보 조회",
        description = "MASTER 권한으로 모든 허브 간 이동 정보를 페이지네이션 없이 조회합니다."
    )
    ResponseEntity<GetHubRouteInfoListResponse> getAllHubRouteInfos();
}