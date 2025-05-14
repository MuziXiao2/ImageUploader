package com.muzixiao2.imageuploader.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/user")
    public String forUser() {
        return "你是登录用户";
    }

    @GetMapping("/admin")
    public String forAdmin() {
        return "你是管理员";
    }
}
