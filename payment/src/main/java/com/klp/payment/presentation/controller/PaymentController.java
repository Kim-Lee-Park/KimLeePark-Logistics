package com.klp.payment.presentation.controller;

import com.klp.payment.application.service.PaymentService;
import com.klp.payment.application.service.dto.PaymentCreateCommand;
import lombok.RequiredArgsConstructor;
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
    public String payment(@RequestBody PaymentCreateCommand command) {
        paymentService.pay(command);
        return "Payment Success";
    }
}
