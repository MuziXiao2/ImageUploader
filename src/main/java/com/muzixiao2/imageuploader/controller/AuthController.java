package com.muzixiao2.imageuploader.controller;

import com.muzixiao2.imageuploader.dto.LoginRequest;
import com.muzixiao2.imageuploader.dto.LoginResponse;
import com.muzixiao2.imageuploader.dto.RegisterRequest;
import com.muzixiao2.imageuploader.security.JwtUtil;
import com.muzixiao2.imageuploader.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AuthController {


    private final UserService userService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok("User registered successfully");
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String accessToken = userService.login(request.getUsername(), request.getPassword());

        // 返回响应
        return ResponseEntity.ok(accessToken);
    }
}
