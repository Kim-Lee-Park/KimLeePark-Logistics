package com.klp.notification.messaging.domain.entity;

import com.klp.notification.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_slack_message", schema = "notification_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "slack_message_id", nullable = false)
    private UUID slackMessageId;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    private SlackMessage(String recipientId, String content, LocalDateTime sentAt) {
        this.recipientId = recipientId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public static SlackMessage create(String recipientId, String content) {
        return new SlackMessage(recipientId, content, null);
    }

    public void sendMessage() {
        this.sentAt = LocalDateTime.now();
    }
}
