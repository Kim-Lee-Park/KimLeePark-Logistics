package com.klp.payment.presentation.controller;

import com.klp.payment.application.service.PaymentService;
import com.klp.payment.application.service.dto.PaymentCreateCommand;
import com.klp.payment.presentation.controller.dto.PaymentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public UUID payment(@RequestBody PaymentCreateCommand command) {
        UUID paymentId = paymentService.pay(command);
        return paymentId;
    }

    @GetMapping
    public List<PaymentResponse> getAll() {
        List<PaymentResponse> response = paymentService.getAll();
        return response;
    }
}
