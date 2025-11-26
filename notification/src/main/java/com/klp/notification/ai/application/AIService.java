package com.klp.notification.ai.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.BusinessException;
import com.klp.notification.ai.application.command.GenerateMessageCommand;
import com.klp.notification.ai.application.dto.DeliveryPlanResponse;
import com.klp.notification.ai.domain.AITextGenerator;
import com.klp.notification.ai.domain.entity.AI;
import com.klp.notification.ai.domain.event.AITextGeneratedEvent;
import com.klp.notification.ai.domain.exception.NotificationErrorCode;
import com.klp.notification.ai.domain.repository.AIRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AITextGenerator textGenerator;
    private final AIRepository aiRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Async
    public void fromTextInput(GenerateMessageCommand command) {
        log.info("AI 텍스트 생성 시작: recipientSlackId={}", command.departureHubManagerId());

        String prompt = buildOrderPrompt(command);
        String aiResponse = textGenerator.generate(prompt);

        log.info("AI 응답 수신 완료");
        DeliveryPlanResponse planResponse = parseAIResponse(aiResponse);

        String formattedMessage = formatDeliveryMessage(command, planResponse);

        AI newAI = AI.create(prompt, aiResponse);
        aiRepository.save(newAI);

        log.info("AI 텍스트 생성 완료, 이벤트 발행: recipientSlackId={}", command.departureHubManagerId());
        eventPublisher.publishEvent(new AITextGeneratedEvent(
            this,
            command.departureHubManagerId(),
            formattedMessage
        ));
    }

    private DeliveryPlanResponse parseAIResponse(String aiResponse) {
        try {
            String jsonContent = extractJsonFromResponse(aiResponse);
            return objectMapper.readValue(jsonContent, DeliveryPlanResponse.class);
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiResponse, e);
            throw new BusinessException(NotificationErrorCode.AI_RESPONSE_PARSE_ERROR);
        }
    }

    private String extractJsonFromResponse(String response) {
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            return response.substring(start, end).trim();
        }
        if (response.contains("{")) {
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}") + 1;
            return response.substring(start, end).trim();
        }
        return response.trim();
    }

    private String formatDeliveryMessage(GenerateMessageCommand command, DeliveryPlanResponse planResponse) {
        StringBuilder message = new StringBuilder();

        message.append("안녕하세요. KLP 물류 배송 시스템입니다. 다음 배송 정보를 확인해주세요.").append("\n");
        message.append("\n");
        message.append("주문 번호 : ").append(command.orderId()).append("\n");
        message.append("주문자 정보 : ").append(command.ordererName())
            .append(" / ").append(command.ordererEmail()).append("\n");
        message.append("주문 시간 : ").append(command.orderTime()).append("\n");
        message.append("상품 정보 : ").append(command.productName())
            .append(" ").append(command.quantity()).append("개").append("\n");

        if (command.requirements() != null && !command.requirements().isBlank()) {
            message.append("요청 사항 : ").append(command.requirements()).append("\n");
        }

        message.append("발송지 : ").append(command.departureHubName()).append("\n");

        if (!command.transitHubNames().isEmpty()) {
            message.append("경유지 : ").append(String.join(", ", command.transitHubNames())).append("\n");
        }

        message.append("도착지 : ").append(command.destinationAddress()).append("\n");
        message.append("배송담당자 : ").append(command.driverName())
            .append(" / ").append(command.driverEmail()).append("\n");
        message.append("\n");
        message.append("위 내용을 기반으로 도출된 최종 발송 시한은 ")
            .append(planResponse.finalDeadline()).append(" 입니다.");

        return message.toString();
    }

    private String buildOrderPrompt(GenerateMessageCommand command) {
        return String.format("""
                당신은 물류 배송 시스템의 배송 계획 전문가입니다.
                아래 주문 정보를 바탕으로 최적의 발송 시한을 계산해주세요.
                
                [주문 정보]
                - 주문 시간: %s
                - 요청 사항: %s
                
                [배송 경로]
                - 출발 허브: %s
                - 경유 허브: %s
                - 최종 목적지: %s
                
                [배송 담당자 정보]
                - 근무 시간: %s
                
                위 정보를 바탕으로 배송 담당자가 근무 시간 내에 상품을 배송할 수 있도록 하는 최적의 발송 시한을 계산하세요.
                
                다음 조건을 고려하세요:
                1. 허브 간 이동은 일반적으로 야간 간선 운송을 통해 이루어지며, 하루에 한 번 운행됩니다.
                2. 각 허브에서의 상하차 및 분류 작업 시간을 고려해야 합니다.
                3. 배송 담당자의 근무 시간 내에 최종 배송이 완료되어야 합니다.
                4. 고객의 요청 사항이 있다면 이를 반영해야 합니다.
                
                응답은 반드시 다음 JSON 형식으로만 답변하세요. 다른 설명은 포함하지 마세요:
                {
                  "finalDeadline": "YYYY년 MM월 DD일 HH시"
                }
                
                예시:
                {
                  "finalDeadline": "2025년 12월 10일 오전 9시"
                }
                """,
            command.orderTime(),
            command.requirements() != null && !command.requirements().isBlank()
                ? command.requirements()
                : "없음",
            command.departureHubName(),
            command.transitHubNames().isEmpty()
                ? "없음 (직배송)"
                : String.join(" → ", command.transitHubNames()),
            command.destinationAddress(),
            command.workingHours()
        );
    }
}
