package com.klp.externalpayments;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external/payment")
public class ExternalPaymentController {

    @PostMapping
    public String payment() {
        try {
            Thread.sleep(1000);
            return "success";
        } catch (InterruptedException e) {
            return "fail";
        }
    }
}
