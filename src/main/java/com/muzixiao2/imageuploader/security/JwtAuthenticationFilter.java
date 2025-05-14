package com.muzixiao2.imageuploader.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.length() <= 7) {
                logger.debug("缺少或无效的 Authorization 头: {}", authHeader);
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            if (token.isBlank()) {
                logger.warn("JWT 令牌为空");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT 令牌为空");
                return;
            }

            logger.debug("处理 JWT 令牌: {}", token);

            // 验证令牌并提取用户名
            if (!jwtUtil.validateToken(token)) {
                logger.warn("无效的 JWT 令牌");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的 JWT 令牌");
                return;
            }

            String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从 JWT 中提取权限
                List<String> roles = jwtUtil.extractRoles(token); // 需在 JwtUtil 中实现
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("用户 {} 认证成功，角色: {}", username, roles);
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT 令牌已过期: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT 令牌已过期，请重新登录或刷新令牌");
        } catch (JwtException e) {
            logger.error("JWT 解析错误: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的 JWT 令牌");
        } catch (Exception e) {
            logger.error("认证过程中发生未知错误: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}