package com.klp.hub.global.config;

import com.klp.hub.global.exception.OrderFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderFeignClientConfig {

    @Bean
    public ErrorDecoder orderFeignErrorDecoder() {
        return new OrderFeignErrorDecoder();
    }
}
