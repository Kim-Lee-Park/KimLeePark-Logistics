package com.klp.notification.ai.application;

import com.klp.notification.ai.application.command.GenerateMessageCommand;
import com.klp.notification.ai.domain.AITextGenerator;
import com.klp.notification.ai.domain.entity.AI;
import com.klp.notification.ai.domain.repository.AIRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AITextGenerator textGenerator;
    private final AIRepository aiRepository;

    public void fromTextInput(GenerateMessageCommand command) {
        String prompt = buildOrderPrompt(command);

        String generatedText = textGenerator.generate(prompt);

        AI newAI = AI.create(prompt, generatedText);
        aiRepository.save(newAI);
    }

    private String buildOrderPrompt(GenerateMessageCommand command) {
        return String.format("""
                [주문 정보]
                - 주문 시간: %s
                - 상품 정보: %s %d,
                - 요청 사항: %s
                - 납기일시: %s
                
                [배송 경로]
                - 출발: %s
                - 경유: %s
                - 도착: %s
                
                배송 담당자 근무 시간: %s
                
                최종 발송 시한을 계산해주세요.
                """,
            command.orderTime(),
            command.productName(),
            command.quantity(),
            command.requirements(),
            command.deliveryDeadline(),
            command.departureHubName(),
            String.join(", ", command.transitHubNames()),
            command.destinationAddress(),
            command.workingHours()
        );
    }
}
