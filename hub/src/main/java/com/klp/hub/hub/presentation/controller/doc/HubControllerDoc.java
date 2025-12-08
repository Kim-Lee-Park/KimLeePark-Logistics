package com.klp.hub.hub.presentation.controller.doc;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.hub.presentation.dto.request.hub.RegisterHubRequest;
import com.klp.hub.hub.presentation.dto.request.hub.UpdateHubRequest;
import com.klp.hub.hub.presentation.dto.response.GetHubByNameResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
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

@Tag(name = "Hub API", description = "허브 관리 API")
public interface HubControllerDoc {

    @Operation(summary = "허브 등록", description = "MASTER 권한으로 허브를 등록합니다.")
    ResponseEntity<RegisterHubResponse> registerHub(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @Valid @RequestBody
        RegisterHubRequest request
    );

    @Operation(summary = "허브 단일 조회", description = "hubId로 허브 상세 정보를 조회합니다.")
    ResponseEntity<GetHubDetailResponse> getHubDetail(
        @Parameter(description = "허브 ID", required = true)
        @PathVariable UUID hubId
    );

    @Operation(summary = "허브 목록 조회", description = "페이지네이션 정보를 이용해 허브 목록을 조회합니다.")
    ResponseEntity<GetHubListResponse> getHubs(
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "허브 수정", description = "MASTER 권한으로 허브 정보를 수정합니다.")
    ResponseEntity<UpdatedHubResponse> updateHub(
        @Parameter(description = "허브 ID", required = true)
        @PathVariable UUID hubId,

        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @RequestBody
        UpdateHubRequest request
    );

    @Operation(summary = "허브 삭제 요청", description = "MASTER 권한으로 허브를 삭제 상태로 마킹합니다.")
    ResponseEntity<Void> deleteHub(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,

        @Parameter(description = "허브 ID", required = true)
        @PathVariable UUID hubId
    );

    @Operation(summary = "허브 이름으로 조회", description = "허브 이름으로 허브를 조회합니다.")
    ResponseEntity<GetHubByNameResponse> getHubByName(
        @Parameter(description = "허브 이름", required = true, example = "서울특별시 센터")
        @RequestParam("name") String hubName
    );
}
