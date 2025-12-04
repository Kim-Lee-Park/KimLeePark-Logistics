package com.klp.payment.payment.infrastructure;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 결제 모킹 서비스 실제 PG사 연동 전까지 결제 성공/실패를 시뮬레이션
 */
@Slf4j
@Service
public class PaymentMockService {

    private static final int SUCCESS_RATE = 80;

    /**
     * 결제 승인 모킹
     */
    public PaymentMockResult mockApprovePayment() {
        int randomValue = ThreadLocalRandom.current().nextInt(100);
        boolean isSuccess = randomValue < SUCCESS_RATE;

        if (isSuccess) {
            String pgTransactionId = "PG_" + UUID.randomUUID().toString().substring(0, 18);
            String billingKey = "BILLING_KEY_" + UUID.randomUUID();
            log.info("[결제 모킹] 결제 승인 성공 - PG 거래 ID: {}", pgTransactionId);
            return PaymentMockResult.success(pgTransactionId, billingKey);
        } else {
            String[] failReasons = {
                "카드 한도 초과",
                "잔액 부족",
                "카드 정보 불일치",
                "승인 거절",
                "네트워크 오류"
            };
            String reason = failReasons[ThreadLocalRandom.current().nextInt(failReasons.length)];
            log.info("[결제 모킹] 결제 승인 실패 - 사유: {}", reason);
            return PaymentMockResult.failure(reason);
        }
    }

    /**
     * 결제 모킹 결과
     */
    public record PaymentMockResult(
        boolean success,
        String pgTransactionId,
        String billingKey,
        String failReason
    ) {

        public static PaymentMockResult success(String pgTransactionId, String billingKey) {
            return new PaymentMockResult(true, pgTransactionId, billingKey, null);
        }

        public static PaymentMockResult failure(String reason) {
            return new PaymentMockResult(false, null, null, reason);
        }
    }
}