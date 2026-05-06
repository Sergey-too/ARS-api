package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/my-crops")
@CrossOrigin(origins = "*")
public class IndividualUserCropController {

    @Autowired
    private IndividualUserCropRepository repository;
    
    @Autowired
    private UserCropRepository userCropRepository;

    @GetMapping("/{id}")
    public ResponseEntity<IndividualUserCrop> getCropById(@PathVariable Integer id) {
        Optional<IndividualUserCrop> crop = repository.findById(id);
        return crop.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<IndividualUserCrop>> getPersonalCrops(@PathVariable Integer userId) {
        return ResponseEntity.ok(repository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @PostMapping
    public ResponseEntity<IndividualUserCrop> createCrop(@RequestBody IndividualUserCrop crop) {
        crop.setCreatedAt(java.time.LocalDateTime.now());
        return new ResponseEntity<>(repository.save(crop), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<IndividualUserCrop> updateCrop(@PathVariable Integer id, @RequestBody IndividualUserCrop crop) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        crop.setId(id);
        return ResponseEntity.ok(repository.save(crop));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCrop(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<IndividualUserCrop> crop = repository.findById(id);
            if (crop.isEmpty()) {
                response.put("success", false);
                response.put("error", "Растение не найдено");
                return ResponseEntity.status(404).body(response);
            }
            
            // Удаляем связи в user_crops
            List<UserCrop> userCrops = userCropRepository.findByIndividualCropId(id);
            if (!userCrops.isEmpty()) {
                userCropRepository.deleteAll(userCrops);
                response.put("deleted_links", userCrops.size());
            }
            
            repository.deleteById(id);
            
            response.put("success", true);
            response.put("message", "Растение удалено");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}