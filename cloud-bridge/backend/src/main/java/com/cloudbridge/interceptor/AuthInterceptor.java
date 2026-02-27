package com.cloudbridge.interceptor;

import com.cloudbridge.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow OPTIONS requests for CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                // Store user info in request attribute for controllers
                request.setAttribute("userId", jwtUtil.extractUserId(token));
                request.setAttribute("username", jwtUtil.extractUsername(token));
                request.setAttribute("role", jwtUtil.extractRole(token));
                return true;
            }
        }

        // Allow GET requests to proceed without auth (public access)
        // But POST/PUT/DELETE require auth
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
