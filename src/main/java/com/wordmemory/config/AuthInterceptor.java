package com.wordmemory.config;

import com.wordmemory.common.BusinessException;
import com.wordmemory.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTR = "currentUserId";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或 token 无效");
        }

        String token = authHeader.substring(7);
        Long userId = authService.validateToken(token);
        if (userId == null) {
            throw new BusinessException(401, "未登录或 token 无效");
        }

        request.setAttribute(USER_ID_ATTR, userId);
        return true;
    }
}
