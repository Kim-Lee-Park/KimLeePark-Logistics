package com.klp.promotion.grade.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradeUpdateScheduler {

    @Scheduled(cron = "0 0 0 1 * ?")
    public void updateCustomerGrades() {
        log.info("등급 갱신 스케줄러 시작");

        try {
            // TODO: 주문 서비스와 연동하여 고객별 지난 1년간 결제 완료 금액 집계
            // TODO: 집계된 금액을 기반으로 GradeType.calculateGrade()를 사용하여 등급 산정
            // TODO: 고객의 등급을 업데이트하고 히스토리 기록

            log.info("등급 갱신 스케줄러 완료");
        } catch (Exception e) {
            log.error("등급 갱신 스케줄러 실행 중 오류 발생", e);
        }
    }
}
