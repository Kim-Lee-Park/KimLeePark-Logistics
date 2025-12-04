package com.klp.promotion.coupon.application.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;


@Slf4j
@RequiredArgsConstructor
public class UserCouponScheduler {


    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredUserCoupons() {
        log.info("만료된 유저 쿠폰 업데이트 스케줄러 시작");

        try {

            //TODO: 유효기간 만료시 업데이트 스케줄러

            log.info("만료된 유저 쿠폰 업데이트 스케줄러 완료");
        } catch (Exception e) {
            log.error("만료된 유저 쿠폰 업데이트 스케줄러 실행 중 오류 발생", e);
        }
    }

}
