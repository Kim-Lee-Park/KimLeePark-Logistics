package com.klp.delivery.global.config;

import com.klp.delivery.common.entity.UserDetailsImpl;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignUserHeaderConfig {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Bean
    public RequestInterceptor userHeaderRelayInterceptor() {
        return template -> {
            SecurityContext context = SecurityContextHolder.getContext();
            if (context == null) {
                return;
            }

            Authentication authentication = context.getAuthentication();
            if (authentication == null
                || !(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
                return;
            }

            template.header(USER_ID_HEADER, String.valueOf(principal.getUserId()));
            template.header(USER_NAME_HEADER, principal.getUsername());
            template.header(USER_ROLE_HEADER, principal.getRole());
        };
    }
}
