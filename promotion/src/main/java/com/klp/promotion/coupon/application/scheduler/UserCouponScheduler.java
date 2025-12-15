package com.klp.promotion.coupon.application.scheduler;


import com.klp.promotion.coupon.application.facade.UserCouponFacade;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserCouponScheduler {

    private final UserCouponFacade userCouponFacade;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredUserCoupons() {
        log.info("만료된 유저 쿠폰 업데이트 스케줄러 시작");

        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            userCouponFacade.markExpiredCoupons(todayStart);

            log.info("만료된 유저 쿠폰 업데이트 스케줄러 완료");
        } catch (Exception e) {
            log.error("만료된 유저 쿠폰 업데이트 스케줄러 실행 중 오류 발생", e);
        }
    }

}
