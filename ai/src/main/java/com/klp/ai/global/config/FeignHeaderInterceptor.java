package com.klp.ai.global.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 호출 시 현재 요청의 인증 헤더를 전달하는 인터셉터
 */
@Component
public class FeignHeaderInterceptor implements RequestInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_NAME_HEADER = "X-User-Name";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        String userId = request.getHeader(USER_ID_HEADER);
        String userName = request.getHeader(USER_NAME_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        if (userId != null) {
            template.header(USER_ID_HEADER, userId);
        }
        if (userName != null) {
            template.header(USER_NAME_HEADER, userName);
        }
        if (userRole != null) {
            template.header(USER_ROLE_HEADER, userRole);
        }
    }
}
