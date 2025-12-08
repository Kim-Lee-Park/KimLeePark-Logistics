package com.klp.delivery.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithMockUserSecurityContextFactory.class)
public @interface CustomWithMockUser {

    long userId() default 1L;

    String username() default "testUser";

    String authority() default "MASTER";
}