package org.andy.democloudgatewayresource.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableMethodSecurity
public class AppController {

    @GetMapping("/secret")
    public String secret() {
        return "secret for you";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('SCOPE_u.test')")
    public String admin() {
        return "admin for you";
    }
}
