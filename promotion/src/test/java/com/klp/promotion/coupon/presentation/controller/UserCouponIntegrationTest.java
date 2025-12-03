package com.klp.promotion.coupon.presentation.controller;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.CouponType;
import com.klp.promotion.coupon.infrastructure.repository.CouponJpaRepositroy;
import com.klp.promotion.coupon.infrastructure.repository.UserCouponJpaRepotiory;
import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserCouponIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CouponJpaRepositroy couponJpaRepositroy;

    @Autowired
    private UserCouponJpaRepotiory userCouponJpaRepotiory;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/v1/user-coupons";
    }

    @AfterEach
    @Transactional
    void tearDown() {
        userCouponJpaRepotiory.deleteAll();
        couponJpaRepositroy.deleteAll();

        Set<String> keys = redisTemplate.keys("coupon:stock:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void createCoupon_Success() {
        // given: 쿠폰 생성 및 저장
        Long userId = 1L;

        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        String key = "coupon:stock:" + coupon.getCouponId();
        redisTemplate.opsForValue().set(key, "10");

        // when
        ExtractableResponse<Response> response = given()
            .header("X-USER-ID", userId)
            .contentType(ContentType.JSON)
            .body("\"" + coupon.getCouponId().toString() + "\"")
            .when()
            .post()
            .then()
            .statusCode(200)
            .extract();

        // then
        UUID userCouponId = UUID.fromString(response.jsonPath().getString("userCouponId"));
        assertThat(response.jsonPath().getString("couponId")).isEqualTo(coupon.getCouponId().toString());

        UserCoupon userCoupon = userCouponJpaRepotiory.findByUserIdAndCouponId(userId, coupon.getCouponId());
        assertThat(userCoupon).isNotNull();
        assertThat(userCoupon.getUserCouponId()).isEqualTo(userCouponId);

        String remainStock = redisTemplate.opsForValue().get(key);
        assertThat(remainStock).isEqualTo("9");
    }

    @Test
    @DisplayName("중복 쿠폰 발급 실패")
    void createCoupon_Fail_AlreadyIssued() {
        // given 쿠폰 생성 및 저장
        Long userId = 1L;

        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        // 이미 발급된 쿠폰 생성
        UserCoupon existingUserCoupon = UserCoupon.create(coupon.getCouponId(), userId);
        userCouponJpaRepotiory.save(existingUserCoupon);

        String key = "coupon:stock:" + coupon.getCouponId();
        redisTemplate.opsForValue().set(key, "10");

        // when & then
        given()
            .header("X-USER-ID", userId)
            .contentType(ContentType.JSON)
            .body("\"" + coupon.getCouponId().toString() + "\"")
            .when()
            .post()
            .then()
            .statusCode(400)
            .extract();

    }

    @Test
    @DisplayName("재고 부족으로 쿠폰 발급 실패")
    void createCoupon_Fail_OutOfStock() {
        // given: 쿠폰 생성 및 저장
        Long userId = 1L;

        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        // Redis 재고 0으로 설정
        String key = "coupon:stock:" + coupon.getCouponId();
        redisTemplate.opsForValue().set(key, "0");

        // when & then
        given()
            .header("X-USER-ID", userId)
            .contentType(ContentType.JSON)
            .body("\"" + coupon.getCouponId().toString() + "\"")
            .when()
            .post()
            .then()
            .statusCode(400)
            .extract();
    }
}

