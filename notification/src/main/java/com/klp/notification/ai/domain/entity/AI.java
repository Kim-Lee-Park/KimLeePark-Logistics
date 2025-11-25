package com.klp.notification.ai.domain.entity;

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
@Table(name = "p_ai", schema = "notification_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AI extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_id", nullable = false)
    private UUID id;

    @Column(name = "input_text", nullable = false, columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output_text", nullable = false, columnDefinition = "TEXT")
    private String outputText;

    @Column(name = "publish", nullable = false)
    private boolean publish;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    private AI(String inputText, String outputText, boolean publish) {
        this.inputText = inputText;
        this.outputText = outputText;
        this.publish = publish;
    }

    public static AI create(String inputText, String outputText) {
        return new AI(inputText, outputText, false);
    }

    public void publish() {
        this.publish = true;
        this.publishedAt = LocalDateTime.now();
    }
}
