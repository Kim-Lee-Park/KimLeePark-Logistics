package com.klp.order.domain.entity.saga;

import com.klp.order.domain.entity.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_saga")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saga_id")
    private UUID sagaId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;

    @Column(name = "current_step", nullable = false)
    private Integer currentStep;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static OrderSaga create(Order order) {
        OrderSaga saga = new OrderSaga();
        saga.order = order;
        saga.status = SagaStatus.STARTED;
        saga.currentStep = 0;
        saga.startedAt = LocalDateTime.now();
        return saga;
    }

    public void updateStatus(SagaStatus newStatus, Integer step) {
        this.status = newStatus;
        this.currentStep = step;

        if (newStatus == SagaStatus.COMPLETED || newStatus == SagaStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public void recordError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void startCompensation() {
        this.status = SagaStatus.COMPENSATING;
    }

    public void completeCompensation() {
        this.status = SagaStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now();
    }

    public void failCompensation(String errorMessage) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
