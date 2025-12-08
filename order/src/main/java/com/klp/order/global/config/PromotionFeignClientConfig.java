package com.klp.order.global.config;

import com.klp.order.global.exception.PromotionFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromotionFeignClientConfig {

    @Bean
    public ErrorDecoder promotionFeignErrorDecoder() {
        return new PromotionFeignErrorDecoder();
    }
}
