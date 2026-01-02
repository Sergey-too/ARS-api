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
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String requestURI = request.getRequestURI();
        
        // Пропускаем публичные эндпоинты
        if (publicEndpoints.contains(requestURI)) {
            return true;
        }
        
        // Проверяем токен для защищенных эндпоинтов
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // Здесь можно добавить проверку токена
        // String token = authHeader.substring(7);
        // if (!isValidToken(token)) {
        //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        //     return false;
        // }
        
        return true;
    }
}