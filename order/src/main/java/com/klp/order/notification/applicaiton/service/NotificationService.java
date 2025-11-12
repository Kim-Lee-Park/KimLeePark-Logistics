package com.klp.order.notification.applicaiton.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void notification(String message) {
        log.info("알림 발송 [슬랙]");
    }
}
