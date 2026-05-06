package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/crops")
@CrossOrigin(origins = "*")
public class UserCropController {
    
    @Autowired private UserCropRepository userCropRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private CropRepository cropRepository;
    @Autowired private IndividualUserCropRepository individualRepo;

    // 1. Добавление растения пользователю
    @PostMapping("/user/add")
    public ResponseEntity<Map<String, Object>> addUserCrop(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = (Integer) request.get("userId");
            Integer areaId = (Integer) request.get("areaId");
            Integer cropId = (Integer) request.get("cropId"); // Системное
            Integer individualId = (Integer) request.get("individualCropId"); // Личное

            UserCrop userCrop = new UserCrop();
            userCrop.setUserId(userId);
            userCrop.setAreaId(areaId);
            userCrop.setPlantedAt(LocalDateTime.now());

            if (cropId != null) {
                userCrop.setCropId(cropId);
            } else if (individualId != null) {
                userCrop.setIndividualCropId(individualId);
            } else {
                throw new Exception("Не указано растение");
            }

            userCropRepository.save(userCrop);
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 2. Получение растений пользователя (с деталями)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCrop>> getUserCrops(@PathVariable Integer userId) {
        try {
            // Используем метод с Join Fetch для подгрузки деталей
            List<UserCrop> userCrops = userCropRepository.findByUserIdWithDetails(userId);
            System.out.println("Найдено растений для пользователя " + userId + ": " + userCrops.size());
            return ResponseEntity.ok(userCrops);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // 3. Удаление конкретного растения у пользователя
    @DeleteMapping("/user/{userId}/{cropId}")
    public ResponseEntity<Map<String, Object>> deleteUserCrop(@PathVariable Integer userId, @PathVariable Integer cropId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<UserCrop> userCrops = userCropRepository.findByUserId(userId);
            Optional<UserCrop> userCropToDelete = userCrops.stream()
                .filter(uc -> uc.getCropId() != null && uc.getCropId().equals(cropId))
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

    // 4. Удаление ВСЕХ растений пользователя
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

    // 5. Получение изображения
    @GetMapping("/img/{filename:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) {
        try {
            File file = new File("uploads/" + filename);
            if (file.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(Files.readAllBytes(file.toPath()));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}