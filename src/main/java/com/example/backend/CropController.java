package com.example.backend;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CropController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CropRepository cropRepository;
    
    // 1. Получить все категории (без растений)
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAllOrdered();
        
        // Очищаем crops в каждой категории чтобы избежать рекурсии
        categories.forEach(category -> category.setCrops(null));
        
        return ResponseEntity.ok(categories);
    }
    
    // 2. Получить растения по названию категории
    @GetMapping("/crops/by-category/{categoryName}")
    public ResponseEntity<List<Crop>> getCropsByCategory(@PathVariable String categoryName) {
        List<Crop> crops = cropRepository.findByCategoryName(categoryName);
        
        // Очищаем category в каждом растении чтобы избежать рекурсии
        crops.forEach(crop -> crop.setCategory(null));
        
        return ResponseEntity.ok(crops);
    }
    
    // 3. Тест
    @GetMapping("/test")
    public String test() {
        return "API работает!";
    }
}