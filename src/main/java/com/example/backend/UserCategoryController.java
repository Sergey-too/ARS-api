package com.example.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-categories")
@CrossOrigin(origins = "*")
public class UserCategoryController {
    
    @Autowired private UserCategoryRepository userCategoryRepository;
    @Autowired private IndividualUserCropRepository individualRepo;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCategory>> getUserCategories(@PathVariable Integer userId) {
        return ResponseEntity.ok(userCategoryRepository.findByUserId(userId));
    }
    
    @PostMapping
    public ResponseEntity<UserCategory> createCategory(@RequestBody UserCategory category) {
        if (userCategoryRepository.existsByUserIdAndName(category.getUserId(), category.getName())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userCategoryRepository.save(category));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        individualRepo.setUserCategoryIdToNull(id);
        userCategoryRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserCategory> updateCategory(@PathVariable Integer id, @RequestBody UserCategory category) {
        return userCategoryRepository.findById(id)
            .map(existing -> {
                existing.setName(category.getName());
                return ResponseEntity.ok(userCategoryRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
}