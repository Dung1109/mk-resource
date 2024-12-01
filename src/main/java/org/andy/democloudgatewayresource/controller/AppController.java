package org.andy.democloudgatewayresource.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping("/secret")
    public String secret() {
        return "secret for you";
    }
}
