package com.klp.global.authorization;

import com.klp.user.domain.enums.UserRole;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithMockUserSecurityContextFactory.class)
public @interface CustomWithMockUser {

    long userId() default 1L;

    String username() default "testUser";

    UserRole authority() default UserRole.MASTER;
}