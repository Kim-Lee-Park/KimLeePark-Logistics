package com.klp.notification.ai.presentation.dto.request;

import com.klp.notification.ai.application.command.GenerateMessageCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record GenerateMessageRequest(
    @NotBlank(message = "발송 허브 담당자 Slack ID는 필수입니다.")
    String departureHubManagerId,

    @NotNull(message = "주문 시간은 필수입니다.")
    LocalDateTime orderTime,

    @NotBlank(message = "상품명은 필수입니다.")
    String productName,

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    Integer quantity,

    String requirements,

    @NotNull(message = "배송 마감 시간은 필수입니다.")
    LocalDateTime deliveryDeadline,

    @NotBlank(message = "출발 허브 이름은 필수입니다.")
    String departureHubName,

    @NotEmpty(message = "경유 허브 목록은 비어있을 수 없습니다.")
    List<String> transitHubNames,

    @NotBlank(message = "목적지 주소는 필수입니다.")
    String destinationAddress,

    String workingHours
) {

    public GenerateMessageCommand toCommand() {
        return new GenerateMessageCommand(
            departureHubManagerId,
            orderTime,
            productName,
            quantity,
            requirements,
            deliveryDeadline,
            departureHubName,
            transitHubNames,
            destinationAddress,
            workingHours != null ? workingHours : "09:00-18:00"
        );
    }
}
