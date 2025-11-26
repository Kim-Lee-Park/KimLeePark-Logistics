package com.klp.notification.ai.infrastructure;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlackMessageSender {

    private final MethodsClient slackClient;

    public void sendDirectMessage(String slackId, String message) {
        try {
            ChatPostMessageResponse response = slackClient.chatPostMessage(req -> req
                .channel(slackId)
                .text(message)
            );

            if (!response.isOk()) {
                throw new RuntimeException("Error sending Slack message: " + response.getError());
            }

        } catch (IOException | SlackApiException e) {
            throw new RuntimeException("Failed to send Slack message", e);
        }
    }
}
