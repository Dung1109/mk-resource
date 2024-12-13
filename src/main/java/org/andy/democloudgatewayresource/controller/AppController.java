package org.andy.democloudgatewayresource.controller;

import lombok.extern.slf4j.Slf4j;
import org.andy.democloudgatewayresource.dto.UserCredentialsDTO;
import org.andy.democloudgatewayresource.dto.UserRequestDTO;
import org.andy.democloudgatewayresource.dto.UserinfoRequestDto;
import org.andy.democloudgatewayresource.dto.UserinfoResponseDTO;
import org.andy.democloudgatewayresource.record.User;
import org.andy.democloudgatewayresource.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class AppController {

    private final UserService userService;

    public AppController(UserService userService) {
        this.userService = userService;
    }

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
                "scope", tokenAttributes.containsKey("scope") ?
                        tokenAttributes.get("scope").toString() : ""
        );
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserinfoResponseDTO> getUser(@PathVariable String username) {
        UserinfoResponseDTO user = userService.getUserByUsername(username);
        log.info("user: {}", user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getEmployee(
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String filterBy,
            @RequestParam(defaultValue = "") String filterRole) {
        try {
            if (filterRole.equals("ALL")) {
                filterRole = "";
            }
            Map<String, Object> response = userService.getUsersPage(
                    pageNo, pageSize, filterBy, filterRole);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching employees", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch employees: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/users/add")
    public ResponseEntity<UserCredentialsDTO> createUser(@RequestBody UserRequestDTO userRequest) {
        UserCredentialsDTO credentials = userService.createUser(userRequest);
        return ResponseEntity.ok(credentials);
    }

    @PutMapping("/users/update/{username}")
    public ResponseEntity<?> updateUser(@PathVariable String username, @Validated @RequestBody UserinfoRequestDto userRequestDTO) {
        userService.updateUser(username, userRequestDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/delete/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok().build();
    }
}
