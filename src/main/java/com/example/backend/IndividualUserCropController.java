package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-crops") 
public class IndividualUserCropController {

    @Autowired
    private IndividualUserCropRepository repository;

    // Получить только кастомные растения юзера
    @GetMapping("/user/{userId}")
    public List<IndividualUserCrop> getPersonalCrops(@PathVariable Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Создать новое личное растение (тот самый POST для "Форда")
    @PostMapping
    public ResponseEntity<IndividualUserCrop> createCrop(@RequestBody IndividualUserCrop crop) {
        return new ResponseEntity<>(repository.save(crop), HttpStatus.CREATED);
    }
    
    // Удаление и обновление (по желанию)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable Integer id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}