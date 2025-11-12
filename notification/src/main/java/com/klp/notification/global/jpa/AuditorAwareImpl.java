package com.klp.notification.global.jpa;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j(topic = "AuditorAwareImpl")
@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public Optional<Long> getCurrentAuditor() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return Optional.empty();
            }

            HttpServletRequest request = attributes.getRequest();
            String userIdHeader = request.getHeader(USER_ID_HEADER);

            if (userIdHeader == null || userIdHeader.isEmpty()) {
                log.warn("유저 ID 관련 헤더가 존재하지 않습니다. request: {}", request.getRequestURI());
                return Optional.empty();
            }

            Long userId = Long.parseLong(userIdHeader.trim());
            log.debug("현재 Auditor 정보: {}", userId);
            return Optional.of(userId);
        } catch (NumberFormatException e) {
            log.error("유저 ID 관련 값이 올바르지 않습니다: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Auditor 정보를 확인할 수 없습니다.", e);
            return Optional.empty();
        }
    }
}