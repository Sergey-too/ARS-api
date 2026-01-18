    package com.example.backend;

    import java.io.File;
    import java.nio.file.Files;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.CrossOrigin;
    import org.springframework.web.bind.annotation.DeleteMapping;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/crops")
    @CrossOrigin(origins = "*")
    public class UserCropController {
        
        @Autowired
        private UserCropRepository userCropRepository;
        
        @Autowired
        private CropRepository cropRepository;
        
        // 1. Получить все растения пользователя
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<UserCrop>> getUserCrops(@PathVariable Integer userId) {
            // Используй метод с загрузкой растения
            List<UserCrop> userCrops = userCropRepository.findByUserIdWithCropDetails(userId);
            
            System.out.println("Отправляем " + userCrops.size() + " растений для пользователя " + userId);
            
            return ResponseEntity.ok(userCrops);
        }
        
        // 2. Добавить растение пользователю
        @PostMapping("/user/add")
        public ResponseEntity<Map<String, Object>> addUserCrop(@RequestBody Map<String, Object> request) {
            Map<String, Object> response = new HashMap<>();
            
            try {
                // Получаем данные из запроса
                Integer userId = (Integer) request.get("userId");
                Integer cropId = (Integer) request.get("cropId");
                
                System.out.println("Получен запрос на добавление растения: userId=" + userId + ", cropId=" + cropId);
                
                // Проверяем обязательные поля
                if (userId == null) {
                    response.put("success", false);
                    response.put("error", "Не указан ID пользователя");
                    return ResponseEntity.badRequest().body(response);
                }
                
                if (cropId == null) {
                    response.put("success", false);
                    response.put("error", "Не указано растение");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Проверяем существует ли растение в БД
                boolean cropExists = cropRepository.existsById(cropId);
                if (!cropExists) {
                    response.put("success", false);
                    response.put("error", "Растение с ID " + cropId + " не найдено");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Проверяем, не добавлено ли уже это растение пользователю
                List<UserCrop> existingCrops = userCropRepository.findByUserId(userId);
                boolean alreadyAdded = existingCrops.stream()
                    .anyMatch(uc -> uc.getCropId().equals(cropId));
                
                if (alreadyAdded) {
                    response.put("success", false);
                    response.put("error", "Это растение уже добавлено в вашу коллекцию");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Создаем новую запись (ТОЛЬКО userId и cropId!)
                UserCrop userCrop = new UserCrop();
                userCrop.setUserId(userId);
                userCrop.setCropId(cropId);
                
                // Сохраняем в БД
                UserCrop savedUserCrop = userCropRepository.save(userCrop);
                
                System.out.println("Растение успешно добавлено. ID записи: " + savedUserCrop.getId());
                
                // Возвращаем успешный ответ
                response.put("success", true);
                response.put("message", "Растение успешно добавлено");
                response.put("userCropId", savedUserCrop.getId());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("error", "Внутренняя ошибка сервера: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }
        
        // 3. Удалить растение у пользователя
            @DeleteMapping("/user/{userId}/{cropId}")
        public ResponseEntity<Map<String, Object>> deleteUserCrop(
                @PathVariable Integer userId, 
                @PathVariable Integer cropId) {
            
            Map<String, Object> response = new HashMap<>();
            
            try {
                // Находим запись для удаления
                List<UserCrop> userCrops = userCropRepository.findByUserId(userId);
                Optional<UserCrop> userCropToDelete = userCrops.stream()
                    .filter(uc -> uc.getCropId().equals(cropId))
                    .findFirst();
                
                if (!userCropToDelete.isPresent()) {
                    response.put("success", false);
                    response.put("error", "Растение не найдено в вашей коллекции");
                    return ResponseEntity.badRequest().body(response);
                }
                
                // Удаляем запись
                userCropRepository.delete(userCropToDelete.get());
                
                response.put("success", true);
                response.put("message", "Растение успешно удалено из коллекции");
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                e.printStackTrace();
                response.put("success", false);
                response.put("error", "Ошибка при удалении: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }

        @GetMapping("/img/{filename:.+}")
        public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws Exception {
            System.out.println("=== GET IMAGE: " + filename + " ===");
            
            // Путь к файлу
            String filePath = "uploads/" + filename;
            System.out.println("Looking for file: " + new File(filePath).getAbsolutePath());
            
            File file = new File(filePath);
            if (file.exists()) {
                System.out.println("✓ File FOUND, size: " + file.length() + " bytes");
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            } else {
                System.out.println("✗ File NOT found at: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
        }
        @DeleteMapping("/user/all/{userId}")
        public ResponseEntity<Map<String, Object>> deleteAllUserCrops(@PathVariable Integer userId) {
            Map<String, Object> response = new HashMap<>();
        
            try {
                System.out.println("Запрос на удаление ВСЕХ растений для пользователя: " + userId);
                
                // Получаем все растения пользователя
                List<UserCrop> userCrops = userCropRepository.findByUserId(userId);
                
                if (userCrops.isEmpty()) {
                    response.put("success", false);
                    response.put("error", "У пользователя нет растений в коллекции");
                    return ResponseEntity.badRequest().body(response);
                }
                
                System.out.println("Найдено растений для удаления: " + userCrops.size());
                
                // Удаляем все записи
                userCropRepository.deleteAll(userCrops);
                
                response.put("success", true);
                response.put("message", "Удалено " + userCrops.size() + " растений");
                response.put("deletedCount", userCrops.size());
                
                System.out.println("Успешно удалено растений: " + userCrops.size());
                
                return ResponseEntity.ok(response);
                
            } catch (Exception e) {
                System.out.println("Ошибка при удалении всех растений: " + e.getMessage());
                e.printStackTrace();
                
                response.put("success", false);
                response.put("error", "Ошибка сервера: " + e.getMessage());
                return ResponseEntity.internalServerError().body(response);
            }
        }
}