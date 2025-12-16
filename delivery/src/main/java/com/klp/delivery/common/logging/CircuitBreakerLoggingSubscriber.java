package com.klp.delivery.common.logging;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerLoggingSubscriber {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final Set<String> attached = ConcurrentHashMap.newKeySet();

    @PostConstruct
    public void subscribeAll() {
        // 현재 존재하는 인스턴스 attach
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(this::attach);

        // 이후 생성되는 인스턴스 attach
        circuitBreakerRegistry.getEventPublisher().onEntryAdded(e -> attach(e.getAddedEntry()));

//        log.warn("[CB] subscribed. existing={}",
//            circuitBreakerRegistry.getAllCircuitBreakers().size());
    }

    private void attach(CircuitBreaker cb) {
        if (!attached.add(cb.getName())) {
            return;
        }

        cb.getEventPublisher()
            .onStateTransition(
                ev -> log.warn("[CB] {} {}", ev.getCircuitBreakerName(), ev.getStateTransition()))
            .onCallNotPermitted(
                ev -> log.warn("[CB] {} CALL_NOT_PERMITTED", ev.getCircuitBreakerName()))
            .onError(ev -> log.error("[CB] {} ERROR ex={}",
                ev.getCircuitBreakerName(),
                ev.getThrowable() == null ? "null" : ev.getThrowable().getClass().getSimpleName()));

//        log.warn("[CB] attached name={}", cb.getName());
    }
}