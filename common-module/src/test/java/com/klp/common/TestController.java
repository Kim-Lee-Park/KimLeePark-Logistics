package com.klp.common;

import com.klp.common.security.model.UserDetailsImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public String any() {
        return "test";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "admin";
    }

    @GetMapping("/auth")
    public String getAuthentication(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return "guest";
        }

        return userDetails.getUserId() + "," + userDetails.getUsername() + "," + userDetails.getRole();
    }
}
