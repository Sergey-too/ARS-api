package com.example.backend;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crops")
@CrossOrigin(origins = "*")
public class UserCropController {
    
    @Autowired
    private UserCropRepository userCropRepository;

    @Autowired
    private AreaRepository areaRepository;
    
    @Autowired
    private CropRepository cropRepository;

    @PostMapping("/user/add")
    public ResponseEntity<Map<String, Object>> addUserCrop(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = Integer.valueOf(request.get("userId").toString());
            Integer cropId = Integer.valueOf(request.get("cropId").toString());
            Integer areaId = Integer.valueOf(request.get("areaId").toString()); 

            System.out.println("Добавление растения: userId=" + userId + ", cropId=" + cropId + ", areaId=" + areaId);

            if (!areaRepository.existsById(areaId)) {
                response.put("success", false);
                response.put("error", "Участок не найден");
                return ResponseEntity.badRequest().body(response);
            }

            if (!cropRepository.existsById(cropId)) {
                response.put("success", false);
                response.put("error", "Растение не найдено");
                return ResponseEntity.badRequest().body(response);
            }

            // Проверка на дубликат (опционально)
            boolean alreadyAdded = userCropRepository.existsByUserIdAndCropId(userId, cropId);
            if (alreadyAdded) {
                response.put("success", false);
                response.put("error", "Это растение уже есть в вашей коллекции");
                return ResponseEntity.badRequest().body(response);
            }

            UserCrop userCrop = new UserCrop();
            userCrop.setUserId(userId);
            userCrop.setCropId(cropId);
            userCrop.setAreaId(areaId); 

            userCropRepository.save(userCrop);

            response.put("success", true);
            response.put("message", "Растение успешно добавлено на участок");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Ошибка сервера: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/user/{userId}/{cropId}")
    public ResponseEntity<Map<String, Object>> deleteUserCrop(@PathVariable Integer userId, @PathVariable Integer cropId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UserCrop> userCrops = userCropRepository.findByUserId(userId);
            Optional<UserCrop> userCropToDelete = userCrops.stream()
                .filter(uc -> uc.getCropId().equals(cropId))
                .findFirst();
            
            if (userCropToDelete.isEmpty()) {
                response.put("success", false);
                response.put("error", "Растение не найдено");
                return ResponseEntity.badRequest().body(response);
            }
            
            userCropRepository.delete(userCropToDelete.get());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/img/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws Exception {
        File file = new File("uploads/" + filename);
        if (file.exists()) {
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .body(Files.readAllBytes(file.toPath()));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCrop>> getUserCrops(@PathVariable Integer userId) {
        try {
            List<UserCrop> userCrops = userCropRepository.findByUserId(userId);
            
            return ResponseEntity.ok(userCrops);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/user/all/{userId}")
    public ResponseEntity<Map<String, Object>> deleteAllUserCrops(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            userCropRepository.deleteByUserId(userId);
            
            response.put("success", true);
            response.put("message", "Все растения пользователя удалены");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", "Ошибка сервера при удалении: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}