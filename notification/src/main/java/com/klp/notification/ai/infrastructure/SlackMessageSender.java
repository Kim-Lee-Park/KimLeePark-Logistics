package com.klp.notification.ai.infrastructure;

import com.klp.notification.ai.domain.MessageSender;
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
        log.debug("Sending Slack message to {}: {}", recipientId, message);

        try {
            ChatPostMessageResponse response = slackClient.chatPostMessage(req -> req
                .channel(recipientId)
                .text(message)
            );

            if (!response.isOk()) {
                log.error("Error sending Slack message: {}", response.getError());
                throw new RuntimeException("Error sending Slack message: " + response.getError());
            }

            log.info("Successfully sent Slack message to {}", recipientId);

        } catch (IOException | SlackApiException e) {
            log.error("Failed to send Slack message", e);
            throw new RuntimeException("Failed to send Slack message", e);
        }
    }
}
