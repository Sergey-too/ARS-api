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
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CropController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CropRepository cropRepository;
    
    // 1. Получить все категории
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAllOrdered();
        categories.forEach(category -> category.setCrops(null));
        return ResponseEntity.ok(categories);
    }
    
    // 2. Получить растения по названию категории (ИСПРАВЛЕНО)
    @GetMapping("/crops/by-category/{categoryName}")
    public ResponseEntity<List<Crop>> getCropsByCategory(@PathVariable String categoryName) {
        List<Crop> crops = cropRepository.findByCategoryName(categoryName);
        
        // НЕ очищаем category, так как используем @JsonIgnore
        // Просто возвращаем список
        return ResponseEntity.ok(crops);
    }
    
    // 3. ДОБАВИТЬ новое растение
    @PostMapping("/crops")
    public ResponseEntity<Crop> addCrop(@RequestBody CropRequest cropRequest) {
        try {
            Crop crop = new Crop();
            
            // Заполняем основные поля
            crop.setName(cropRequest.getName());
            crop.setDescription(cropRequest.getDescription());
            crop.setMinTemp(cropRequest.getMinTemp());
            crop.setMaxTemp(cropRequest.getMaxTemp());
            crop.setMaxWind(cropRequest.getMaxWind());
            crop.setMinHumidity(cropRequest.getMinHumidity());
            crop.setMaxHumidity(cropRequest.getMaxHumidity());
            crop.setNeededPrecipitation(cropRequest.getNeededPrecipitation());
            crop.setSowingDepth(cropRequest.getSowingDepth());
            crop.setDaysToGermination(cropRequest.getDaysToGermination());
            crop.setDaysToHarvest(cropRequest.getDaysToHarvest());
            crop.setCanSeedlings(cropRequest.getCanSeedlings());
            crop.setCanDirectSow(cropRequest.getCanDirectSow());
            crop.setPhotoPath(cropRequest.getPhotoPath());
            
            // Работаем с категорией
            if (cropRequest.getCategory() != null && !cropRequest.getCategory().isEmpty()) {
                // Ищем категорию по имени
                Category category = categoryRepository.findByName(cropRequest.getCategory());
                if (category != null) {
                    crop.setCategory(category);
                } else {
                    // Создаем новую категорию
                    Category newCategory = new Category();
                    newCategory.setName(cropRequest.getCategory());
                    newCategory = categoryRepository.save(newCategory);
                    crop.setCategory(newCategory);
                }
            }
            
            Crop savedCrop = cropRepository.save(crop);
            return ResponseEntity.ok(savedCrop);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 4. Получить все растения
    @GetMapping("/crops")
    public ResponseEntity<List<Crop>> getAllCrops() {
        List<Crop> crops = cropRepository.findAll();
        return ResponseEntity.ok(crops);
    }
    
    // // 5. Получить растение по ID
    // @GetMapping("/crops/{id}")
    // public ResponseEntity<Crop> getCropById(@PathVariable Integer id) {
    //     return cropRepository.findById(id)
    //             .map(ResponseEntity::ok)
    //             .orElse(ResponseEntity.notFound().build());
    // }
    
    // 6. Обновить растение
    @PutMapping("/crops/{id}")
    public ResponseEntity<Crop> updateCrop(@PathVariable Integer id, 
                                           @RequestBody CropRequest cropRequest) {
        try {
            Crop existingCrop = cropRepository.findById(id).orElse(null);
            if (existingCrop == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Обновляем поля
            existingCrop.setName(cropRequest.getName());
            existingCrop.setDescription(cropRequest.getDescription());
            existingCrop.setMinTemp(cropRequest.getMinTemp());
            existingCrop.setMaxTemp(cropRequest.getMaxTemp());
            existingCrop.setMaxWind(cropRequest.getMaxWind());
            existingCrop.setMinHumidity(cropRequest.getMinHumidity());
            existingCrop.setMaxHumidity(cropRequest.getMaxHumidity());
            existingCrop.setNeededPrecipitation(cropRequest.getNeededPrecipitation());
            existingCrop.setSowingDepth(cropRequest.getSowingDepth());
            existingCrop.setDaysToGermination(cropRequest.getDaysToGermination());
            existingCrop.setDaysToHarvest(cropRequest.getDaysToHarvest());
            existingCrop.setCanSeedlings(cropRequest.getCanSeedlings());
            existingCrop.setCanDirectSow(cropRequest.getCanDirectSow());
            existingCrop.setPhotoPath(cropRequest.getPhotoPath());
            
            // Обновляем категорию если изменилась
            if (cropRequest.getCategory() != null && 
                !cropRequest.getCategory().equals(existingCrop.getCategoryName())) {
                
                Category category = categoryRepository.findByName(cropRequest.getCategory());
                if (category != null) {
                    existingCrop.setCategory(category);
                } else {
                    Category newCategory = new Category();
                    newCategory.setName(cropRequest.getCategory());
                    newCategory = categoryRepository.save(newCategory);
                    existingCrop.setCategory(newCategory);
                }
            }
            
            Crop updatedCrop = cropRepository.save(existingCrop);
            return ResponseEntity.ok(updatedCrop);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 7. Удалить растение
    @DeleteMapping("/crops/{id}")
    public ResponseEntity<Void> deleteCrop(@PathVariable Integer id) {
        try {
            if (!cropRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            cropRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 8. Тест
    @GetMapping("/test")
    public String test() {
        return "API работает!";
    }
    
    // Вспомогательный класс для запроса
    public static class CropRequest {
        private String name;
        private String category;
        private String description;
        private Float minTemp;
        private Float maxTemp;
        private Float maxWind;
        private Integer minHumidity;
        private Integer maxHumidity;
        private Float neededPrecipitation;
        private Integer sowingDepth;
        private Integer daysToGermination;
        private Integer daysToHarvest;
        private Boolean canSeedlings;
        private Boolean canDirectSow;
        private String photoPath;
        
        // Геттеры и сеттеры
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Float getMinTemp() { return minTemp; }
        public void setMinTemp(Float minTemp) { this.minTemp = minTemp; }
        
        public Float getMaxTemp() { return maxTemp; }
        public void setMaxTemp(Float maxTemp) { this.maxTemp = maxTemp; }
        
        public Float getMaxWind() { return maxWind; }
        public void setMaxWind(Float maxWind) { this.maxWind = maxWind; }
        
        public Integer getMinHumidity() { return minHumidity; }
        public void setMinHumidity(Integer minHumidity) { this.minHumidity = minHumidity; }
        
        public Integer getMaxHumidity() { return maxHumidity; }
        public void setMaxHumidity(Integer maxHumidity) { this.maxHumidity = maxHumidity; }
        
        public Float getNeededPrecipitation() { return neededPrecipitation; }
        public void setNeededPrecipitation(Float neededPrecipitation) { 
            this.neededPrecipitation = neededPrecipitation; 
        }
        
        public Integer getSowingDepth() { return sowingDepth; }
        public void setSowingDepth(Integer sowingDepth) { this.sowingDepth = sowingDepth; }
        
        public Integer getDaysToGermination() { return daysToGermination; }
        public void setDaysToGermination(Integer daysToGermination) { 
            this.daysToGermination = daysToGermination; 
        }
        
        public Integer getDaysToHarvest() { return daysToHarvest; }
        public void setDaysToHarvest(Integer daysToHarvest) { this.daysToHarvest = daysToHarvest; }
        
        public Boolean getCanSeedlings() { return canSeedlings; }
        public void setCanSeedlings(Boolean canSeedlings) { this.canSeedlings = canSeedlings; }
        
        public Boolean getCanDirectSow() { return canDirectSow; }
        public void setCanDirectSow(Boolean canDirectSow) { this.canDirectSow = canDirectSow; }
        
        public String getPhotoPath() { return photoPath; }
        public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    }
}