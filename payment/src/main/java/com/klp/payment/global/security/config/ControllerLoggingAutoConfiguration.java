package com.klp.payment.global.security.config;

import com.klp.payment.common.logging.ControllerLoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ControllerLoggingAspect.class)
public class ControllerLoggingAutoConfiguration {

}
