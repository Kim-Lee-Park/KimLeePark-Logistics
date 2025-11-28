package com.klp.delivery.global.config;

import com.klp.delivery.common.exception.DriverFeignClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class DeliveryFeignClientConfig {

    @Bean
    public ErrorDecoder DriverFeignClientErrorDecoder() {
        return new DriverFeignClientErrorDecoder();
    }

}
