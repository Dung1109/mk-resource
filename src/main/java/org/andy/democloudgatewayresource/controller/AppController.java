package org.andy.democloudgatewayresource.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@EnableMethodSecurity
@Slf4j
public class AppController {

    @GetMapping("/secret")
    public String secret() {
        return "secret for you";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin() {
        return "admin role for you";
    }

    @GetMapping("/admin/scope")
    @PreAuthorize("hasAuthority('SCOPE_u.test')")
    public String adminScope() {
        return "scope for you";
    }


    @GetMapping("/access_token")
    public Map<String, String> accessToken(JwtAuthenticationToken jwtToken) {
        Map<String, Object> tokenAttributes = jwtToken.getTokenAttributes();
        log.info("principal class: {}", jwtToken.getPrincipal().getClass());

        var authorities = jwtToken.getAuthorities();
        log.info("authorities: {}", authorities);
        return Map.of(
                "principal", jwtToken.getName(),
                "access_token", jwtToken.getToken().getTokenValue(),
                "authorities", authorities.toString(),
                "scope",tokenAttributes.containsKey("scope") ?
                        tokenAttributes.get("scope").toString() : ""
        );
    }

}
