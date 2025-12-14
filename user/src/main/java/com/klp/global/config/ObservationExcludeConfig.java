package com.klp.global.config;

import io.micrometer.observation.ObservationPredicate;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class ObservationExcludeConfig {

    @Bean
    public ObservationPredicate excludeActuatorPrometheus() {
        return (name, context) -> {
            if (context instanceof ServerRequestObservationContext serverCtx) {
                HttpServletRequest request = serverCtx.getCarrier();
                if (request != null) {
                    String path = request.getRequestURI();
                    String method = request.getMethod();

                    if ("GET".equals(method) && "/actuator/prometheus".contains(path)) {
                        return false;
                    }

                    if (path.contains("/eureka")) {
                        return false;
                    }
                }
            }

            if (context instanceof ClientRequestObservationContext clientCtx) {
                var req = clientCtx.getCarrier();
                if (req != null) {
                    URI uri = req.getURI();
                    String host = uri.getHost();
                    int port = uri.getPort();
                    String path = uri.getPath();

                    if (host != null && path.startsWith("/eureka/")) {
                        return false;
                    }
                }
            }
            return true;
        };
    }
}
