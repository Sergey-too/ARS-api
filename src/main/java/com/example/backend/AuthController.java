package com.example.backend;

import org.springframework.security.crypto.bcrypt.BCrypt; // Используем статический метод для простоты
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            
             if ((login == null || login.trim().isEmpty()) && email != null) {
                login = email.split("@")[0];
                login = login.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            }
            
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
    

            if (userRepository.existsByLogin(login) || userRepository.existsByEmail(email)) {
                response.put("success", false);
                response.put("error", "Пользователь уже существует");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = new User();
            user.setLogin(login);
            user.setEmail(email);

            user.setPassword(password); 
            
            User savedUser = userRepository.save(user);

            response.put("success", true);
            response.put("message", "Регистрация успешна");
            response.put("token", "token_" + savedUser.getId() + "_" + System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String identifier = request.get("identifier");
            String password = request.get("password");
            
            Optional<User> userOpt = userRepository.findByIdentifier(identifier);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "Пользователь не найден");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            User user = userOpt.get();

            if (user.isInBan()) {
                response.put("success", false);
                response.put("error", "Ваш аккаунт заблокирован администратором");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                response.put("success", false);
                response.put("error", "Неверный пароль");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Создаем ответ для Android
            Map<String, Object> userResponse = new HashMap<>();
            userResponse.put("id", user.getId());
            userResponse.put("login", user.getLogin());
            userResponse.put("email", user.getEmail());
            userResponse.put("isAdmin", user.isAdmin()); 
            userResponse.put("createdAt", user.getRegistrationDate());
            
            response.put("success", true);
            response.put("user", userResponse);
            response.put("token", "token_" + user.getId() + "_" + System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Ошибка сервера");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}