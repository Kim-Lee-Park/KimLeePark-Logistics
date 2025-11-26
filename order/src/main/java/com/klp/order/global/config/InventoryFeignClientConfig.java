package com.klp.order.global.config;

import com.klp.order.global.exception.InventoryFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryFeignClientConfig {

    @Bean
    public ErrorDecoder inventoryFeignErrorDecoder() {
        return new InventoryFeignErrorDecoder();
    }
}
