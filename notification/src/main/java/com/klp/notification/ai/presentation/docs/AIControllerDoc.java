package com.klp.notification.ai.presentation.docs;

import com.klp.notification.ai.presentation.dto.request.GenerateMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI Notification API", description = "AI 기반 알림 메시지 생성 API")
public interface AIControllerDoc {

    @Operation(
        summary = "AI 알림 메시지 생성",
        description = """
            AI를 활용하여 배송 관련 알림 메시지를 생성하고 Slack으로 발송합니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "메시지 생성 및 발송 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)"),
        @ApiResponse(responseCode = "500", description = "AI 서비스 또는 Slack 발송 오류")
    })
    ResponseEntity<Void> generateMessage(@Valid @RequestBody GenerateMessageRequest request);
}
