package com.muzixiao2.imageuploader.service;

import com.muzixiao2.imageuploader.controller.AuthController;
import com.muzixiao2.imageuploader.entity.UserEntity;
import com.muzixiao2.imageuploader.repository.UserRepository;
import com.muzixiao2.imageuploader.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(rawPassword));
        userEntity.setRoles(List.of("ROLE_USER"));// 赋予默认角色
        userRepository.save(userEntity);

    }


    public String login(String username, String password) {

        // 验证用户名和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 获取用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 获取用户角色
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(Collectors.toList());

        // 生成访问令牌
        String accessToken = jwtUtil.generateToken(username, roles);

        logger.info("用户 {} 登录成功，角色: {}", username, roles);

        return accessToken;

    }

}