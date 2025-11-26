package com.klp.notification.ai.presentation.dto.request;

import com.klp.notification.ai.application.command.GenerateMessageCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GenerateMessageRequest(
    @NotBlank(message = "발송 허브 담당자 Slack ID는 필수입니다.")
    String departureHubManagerId,

    @NotNull(message = "주문 ID는 필수입니다.")
    UUID orderId,

    @NotBlank(message = "주문자 이름은 필수입니다.")
    String ordererName,

    @NotBlank(message = "주문자 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String ordererEmail,

    @NotNull(message = "주문 시간은 필수입니다.")
    LocalDateTime orderTime,

    @NotBlank(message = "상품명은 필수입니다.")
    String productName,

    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    Integer quantity,

    String requirements,

    @NotBlank(message = "출발 허브 이름은 필수입니다.")
    String departureHubName,

    List<String> transitHubNames,

    @NotBlank(message = "목적지 주소는 필수입니다.")
    String destinationAddress,

    @NotBlank(message = "배송담당자 이름은 필수입니다.")
    String driverName,

    @NotBlank(message = "배송담당자 이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String driverEmail,

    String workingHours
) {

    public GenerateMessageCommand toCommand() {
        return new GenerateMessageCommand(
            departureHubManagerId,
            orderId,
            ordererName,
            ordererEmail,
            orderTime,
            productName,
            quantity,
            requirements,
            departureHubName,
            transitHubNames,
            destinationAddress,
            driverName,
            driverEmail,
            workingHours != null ? workingHours : "09:00-18:00"
        );
    }
}
