package com.klp.hub.global.config;

import com.klp.hub.global.exception.DeliveryFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryFeignClientConfig {

    @Bean
    public ErrorDecoder deliveryFeignErrorDecoder() {
        return new DeliveryFeignErrorDecoder();
    }
}
