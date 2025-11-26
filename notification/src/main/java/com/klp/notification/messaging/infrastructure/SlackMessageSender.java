package com.klp.notification.messaging.infrastructure;

import com.klp.common.exception.BusinessException;
import com.klp.notification.messaging.domain.MessageSender;
import com.klp.notification.messaging.domain.exception.MessagingErrorCode;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackMessageSender implements MessageSender {

    private final MethodsClient slackClient;

    @Override
    public void sendMessage(String recipientId, String message) {
        log.debug("{}에게 슬랙 메시지 전송: {}", recipientId, message);

        try {
            ChatPostMessageResponse response = slackClient.chatPostMessage(req -> req
                .channel(recipientId)
                .text(message)
            );

            if (!response.isOk()) {
                log.error("슬랙 메시지 전송 실패: {}", response.getError());
                throw new BusinessException(MessagingErrorCode.MESSAGE_SENDING_FAILED);
            }

            log.info("{} 배송 담당자에게 슬랙 메시지 전송 성공", recipientId);

        } catch (IOException | SlackApiException e) {
            log.error("슬랙 메시지 전송 실패", e);
            throw new BusinessException(MessagingErrorCode.MESSAGE_SENDING_FAILED);
        }
    }
}
