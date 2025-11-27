package com.klp.notification.messaging.domain.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MessagingErrorCode implements ErrorCode {
    MESSAGE_SENDING_FAILED(HttpStatus.BAD_GATEWAY, "메시지 전송에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
