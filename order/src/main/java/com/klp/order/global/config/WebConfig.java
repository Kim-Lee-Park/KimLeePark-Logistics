package com.klp.order.global.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PageableArgumentResolver pageableArgumentResolver;

    public WebConfig(PageableArgumentResolver pageableArgumentResolver) {
        this.pageableArgumentResolver = pageableArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(pageableArgumentResolver);
    }
}
