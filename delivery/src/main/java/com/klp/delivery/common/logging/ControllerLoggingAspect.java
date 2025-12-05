package com.klp.delivery.common.logging;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Around("""
        (@within(org.springframework.web.bind.annotation.RestController) 
         || @within(org.springframework.stereotype.Controller))
         && execution(public * *(..))
        """)
    public Object logControllerRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("Request to {} with args: {}", method, Arrays.toString(args));
        Object result = joinPoint.proceed();
        log.info("Response from {} : {}", method, result);

        return result;
    }

}
