package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/{id}/toggle-admin")
    public ResponseEntity<?> toggleAdmin(@PathVariable("id") Integer id) { // Исправлено с metalPath
        return userRepository.findById(id).map(user -> {
            user.setAdmin(!user.isAdmin());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/toggle-ban")
    public ResponseEntity<?> toggleBan(@PathVariable("id") Integer id) {
        return userRepository.findById(id).map(user -> {
            user.setInBan(!user.isInBan());
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}