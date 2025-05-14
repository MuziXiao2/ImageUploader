package com.muzixiao2.imageuploader.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> currentUserName(Authentication authentication) {
        return ResponseEntity.ok("当前用户是: " + authentication.getName());
    }
}
