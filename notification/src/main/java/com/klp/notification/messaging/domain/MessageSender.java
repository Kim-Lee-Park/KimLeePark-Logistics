package com.klp.notification.messaging.domain;

public interface MessageSender {

    /**
     * 특정 사용자에게 메시지를 전송합니다.
     */
    void sendMessage(String recipientId, String message);
}
