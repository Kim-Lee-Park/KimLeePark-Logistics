package com.klp.delivery.global.config;

import com.klp.delivery.common.exception.CompanyFeignClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class CompanyFeignClientConfig {

    @Bean
    public ErrorDecoder companyFeignClientErrorDecoder() {
        return new CompanyFeignClientErrorDecoder();
    }

}
