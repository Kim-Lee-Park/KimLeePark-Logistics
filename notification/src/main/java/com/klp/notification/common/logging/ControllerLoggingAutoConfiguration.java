package com.klp.notification.common.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ControllerLoggingAspect.class)
public class ControllerLoggingAutoConfiguration {

}
