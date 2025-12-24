package com.klp.global.config;

import com.klp.global.exception.InventoryFeignErrorDecoder;
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
