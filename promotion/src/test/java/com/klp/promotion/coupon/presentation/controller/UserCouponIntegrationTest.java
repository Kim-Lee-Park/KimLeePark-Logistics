package com.klp.promotion.coupon.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.promotion.global.security.model.UserDetailsImpl;
import com.klp.promotion.coupon.domain.entity.Coupon;
import com.klp.promotion.coupon.domain.entity.UserCoupon;
import com.klp.promotion.coupon.domain.enums.CouponType;
import com.klp.promotion.coupon.domain.enums.UserCouponStatus;
import com.klp.promotion.coupon.infrastructure.repository.CouponJpaRepositroy;
import com.klp.promotion.coupon.infrastructure.repository.UserCouponJpaRepotiory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserCouponIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepositroy couponJpaRepositroy;

    @Autowired
    private UserCouponJpaRepotiory userCouponJpaRepotiory;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

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
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void createCoupon_Success() throws Exception {
        // given: 쿠폰 생성 및 저장
        Long userId = 1L;

        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        String key = "coupon:stock:" + coupon.getCouponId();
        redisTemplate.opsForValue().set(key, "10");

        // when
        MvcResult result = mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(userId, "testuser", "CUSTOMER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon.getCouponId())))
            .andExpect(status().isOk())
            .andReturn();

        // then
        String responseBody = result.getResponse().getContentAsString();
        UUID userCouponId = UUID.fromString(objectMapper.readTree(responseBody).get("userCouponId").asText());
        assertThat(objectMapper.readTree(responseBody).get("couponId").asText()).isEqualTo(coupon.getCouponId().toString());

        UserCoupon userCoupon = userCouponJpaRepotiory.findByUserIdAndCouponId(userId, coupon.getCouponId());
        assertThat(userCoupon).isNotNull();
        assertThat(userCoupon.getUserCouponId()).isEqualTo(userCouponId);

        String remainStock = redisTemplate.opsForValue().get(key);
        assertThat(remainStock).isEqualTo("9");
    }

    @Test
    @DisplayName("중복 쿠폰 발급 실패")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void createCoupon_Fail_AlreadyIssued() throws Exception {
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
        mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(userId, "testuser", "CUSTOMER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon.getCouponId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("재고 부족으로 쿠폰 발급 실패")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void createCoupon_Fail_OutOfStock() throws Exception {
        // given: 쿠폰 생성 및 저장
        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        // Redis 재고 0으로 설정
        String key = "coupon:stock:" + coupon.getCouponId();
        redisTemplate.opsForValue().set(key, "0");

        // when & then
        mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(1L, "testuser", "CUSTOMER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coupon.getCouponId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("권한이 없는 사용자가 쿠폰 발급 실패")
    void createCoupon_Fail_NoAuthorization() throws Exception {
        // given
        UUID couponId = UUID.randomUUID();

        // when & then - 인증 없이 요청
        mockMvc.perform(post("/v1/user-coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("잘못된 권한으로 쿠폰 발급 실패")
    @WithMockUser(username = "testuser", roles = "DRIVER")
    void createCoupon_Fail_InvalidRole() throws Exception {
        // given
        UUID couponId = UUID.randomUUID();

        // when & then - CUSTOMER 권한이 필요한데 DRIVER로 요청
        mockMvc.perform(post("/v1/user-coupons")
                .with(user(new UserDetailsImpl(1L, "testuser", "DRIVER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(couponId)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void useUserCoupon_Success() throws Exception {
        // given: 쿠폰 발급
        Long userId = 1L;
        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        UserCoupon userCoupon = UserCoupon.create(coupon.getCouponId(), userId);
        userCoupon = userCouponJpaRepotiory.save(userCoupon);

        // when
        mockMvc.perform(post("/v1/user-coupons/" + coupon.getCouponId() + "/use")
                .with(user(new UserDetailsImpl(userId, "testuser", "CUSTOMER"))))
            .andExpect(status().isNoContent());

        // then
        UserCoupon updatedUserCoupon = userCouponJpaRepotiory.findByUserCouponId(userCoupon.getUserCouponId());
        assertThat(updatedUserCoupon.getStatus()).isEqualTo(UserCouponStatus.USED);
        assertThat(updatedUserCoupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("유저 쿠폰 삭제 성공")
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    void deleteUserCoupon_Success() throws Exception {
        // given: 쿠폰 발급
        Long userId = 1L;
        Coupon coupon = Coupon.create("테스트 쿠폰", CouponType.RATE, 10L, 10000, 5000L, 100L, 50L,
            LocalDateTime.now().plusDays(30));
        coupon = couponJpaRepositroy.save(coupon);

        UserCoupon userCoupon = UserCoupon.create(coupon.getCouponId(), userId);
        userCoupon = userCouponJpaRepotiory.save(userCoupon);

        // when
        mockMvc.perform(delete("/v1/user-coupons/" + userCoupon.getUserCouponId())
                .with(user(new UserDetailsImpl(userId, "testuser", "CUSTOMER"))))
            .andExpect(status().isNoContent());

        // then
        UserCoupon deletedUserCoupon = userCouponJpaRepotiory.findByUserCouponId(userCoupon.getUserCouponId());
        assertThat(deletedUserCoupon.isDeleted()).isTrue();
    }
}

