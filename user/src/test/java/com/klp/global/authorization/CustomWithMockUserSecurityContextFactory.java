package com.klp.global.authorization;

import com.klp.global.security.model.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomWithMockUserSecurityContextFactory implements WithSecurityContextFactory<CustomWithMockUser> {

    @Override
    public SecurityContext createSecurityContext(CustomWithMockUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserDetailsImpl principal = new UserDetailsImpl(
            annotation.userId(),
            annotation.username(),
            annotation.authority().name()
        );
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        context.setAuthentication(authentication);
        return context;
    }

}