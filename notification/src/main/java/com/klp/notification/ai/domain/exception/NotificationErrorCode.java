package com.klp.notification.ai.domain.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    AI_TEXT_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "AI 텍스트 생성에 실패했습니다."),
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답을 파싱하는데 실패했습니다."),
    MESSAGE_SENDING_FAILED(HttpStatus.BAD_GATEWAY, "메시지 전송에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
