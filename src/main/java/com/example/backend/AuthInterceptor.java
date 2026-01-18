// AuthInterceptor.java
package com.example.backend;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private Set<String> publicEndpoints = new HashSet<>();
    
    public AuthInterceptor() {
        publicEndpoints.add("/api/auth/login");
        publicEndpoints.add("/api/auth/register");
        publicEndpoints.add("/api/weather/test-data");
        publicEndpoints.add("/api/regions/test");
        publicEndpoints.add("/api/files/test");
        publicEndpoints.add("/api/files/public/");
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("Intercepting: " + requestURI);
        
        if (requestURI.startsWith("/api/files/public/")) {
            System.out.println("Public file endpoint, ALLOWING access");
            return true;  // Разрешить без проверки токена
        }
        
        // Проверяем если endpoint в списке публичных
        if (publicEndpoints.contains(requestURI)) {
            System.out.println("Public endpoint (exact match), skipping auth check");
            return true;
        }
        
        // Плюс проверяем по префиксам
        if (requestURI.startsWith("/api/auth/") ||
            requestURI.startsWith("/api/files/public/") ||
            requestURI.startsWith("/public/") ||
            requestURI.equals("/api/weather/test-data") ||
            requestURI.equals("/api/regions/test") ||
            requestURI.equals("/api/files/test")) {  // Добавьте эту строку
            System.out.println("Public endpoint (prefix match), skipping auth check");
            return true;
        }
        
        // Проверяем токен для ВСЕХ остальных запросов
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No auth token, returning 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Authorization required\"}");
            return false;
        }
        
        // Здесь можно добавить реальную проверку токена
        String token = authHeader.substring(7);
        System.out.println("Token: " + token);
        
        // Простая проверка - токен должен начинаться с "token_"
        if (!token.startsWith("token_")) {
            System.out.println("Invalid token format");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        return true;
    }
}