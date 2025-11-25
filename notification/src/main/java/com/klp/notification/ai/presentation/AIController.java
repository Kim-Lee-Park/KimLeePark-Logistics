package com.klp.notification.ai.presentation;

import com.klp.notification.ai.application.AIService;
import com.klp.notification.ai.presentation.dto.request.GenerateMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/generate")
    public ResponseEntity<Void> generateMessage(@RequestBody GenerateMessageRequest request) {
        aiService.fromTextInput(request.toCommand());
        return ResponseEntity.ok().build();
    }
}
