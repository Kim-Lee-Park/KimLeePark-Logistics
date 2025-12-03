package com.klp.user.global.config;

import com.klp.global.exception.CompanyFeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompanyFeignClientConfig {

    @Bean
    public ErrorDecoder companyFeignErrorDecoder() {
        return new CompanyFeignErrorDecoder();
    }
}
