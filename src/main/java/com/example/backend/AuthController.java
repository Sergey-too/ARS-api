package com.example.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String login = request.get("login");
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name"); // Для Android
            
            // Если нет логина, создаем из email
            if ((login == null || login.trim().isEmpty()) && email != null) {
                login = email.split("@")[0];
                login = login.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            }
            
            // Валидация
            if (login == null || login.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Логин обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Email обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Пароль обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Проверяем существование
            if (userRepository.existsByLogin(login)) {
                response.put("success", false);
                response.put("error", "Пользователь с таким логином уже существует");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (userRepository.existsByEmail(email)) {
                response.put("success", false);
                response.put("error", "Пользователь с таким email уже существует");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Создаем пользователя
            User user = new User();
            user.setLogin(login);
            user.setEmail(email);
            user.setPassword(password);
            
            User savedUser = userRepository.save(user);
            
            // Создаем ответ для Android
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", savedUser.getId());
            userResponse.put("name", name != null ? name : login); // Возвращаем name для Android
            userResponse.put("email", savedUser.getEmail());
            userResponse.put("login", savedUser.getLogin());
            userResponse.put("createdAt", savedUser.getRegistrationDate());
            
            response.put("success", true);
            response.put("message", "Регистрация успешна");
            response.put("token", "token_" + savedUser.getId() + "_" + System.currentTimeMillis());
            response.put("user", userResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Ошибка сервера: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String identifier = request.get("identifier");
            String password = request.get("password");
            
            if (identifier == null || identifier.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Логин/email обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "Пароль обязателен");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Ищем пользователя по логину или email
            Optional<User> userOpt = userRepository.findByIdentifier(identifier);
            
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("error", "Пользователь не найден");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();
            
            // Проверяем пароль (упрощенная проверка)
            if (!user.getPasswordHash().equals(password)) {
                response.put("success", false);
                response.put("error", "Неверный пароль");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Создаем ответ для Android
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("name", user.getLogin()); // Используем login как name
            userResponse.put("email", user.getEmail());
            userResponse.put("login", user.getLogin());
            userResponse.put("createdAt", user.getRegistrationDate());
            
            response.put("success", true);
            response.put("message", "Вход выполнен успешно");
            response.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());
            response.put("user", userResponse);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Ошибка сервера: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}