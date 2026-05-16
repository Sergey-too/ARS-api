package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
public class GardenHistoryController {

    @Autowired private GardenHistoryRepository historyRepository;
    @Autowired private UserCropRepository userCropRepository;

    @PostMapping("/plant") 
    public ResponseEntity<?> plantCrop(@RequestBody Map<String, Object> request) {
        try {
            Integer userCropId = (Integer) request.get("userCropId");
            
            UserCrop uc = userCropRepository.findById(userCropId)
                    .orElseThrow(() -> new RuntimeException("Культура не найдена"));

            // Получаем userId хозяина грядки
            Integer userId = uc.getUserId(); 
            String areaName = (uc.getArea() != null) ? uc.getArea().getName() : "Участок";
            
            String name = (uc.getCrop() != null) ? uc.getCrop().getName() : 
                    (uc.getIndividualCrop() != null ? uc.getIndividualCrop().getName() : "Неизвестно");

            // ИСПРАВЛЕНО: Проверяем, посажено ли растение с таким именем У ЭТОГО пользователя НА ЭТОМ участке
            boolean alreadyPlanted = historyRepository.existsByUserIdAndAreaNameAndCropNameAndActionTypeId(
                    userId, areaName, name, 1
            );

            if (alreadyPlanted) {
                return ResponseEntity.badRequest().body(Map.of("error", "Это растение уже посажено на данном участке!"));
            }

            // Создаем запись истории
            GardenHistory history = new GardenHistory();
            history.setUserId(userId); // ИСПРАВЛЕНО: Привязываем к пользователю!
            history.setActionTypeId(1); 
            history.setDoneAt(LocalDateTime.now());
            history.setAreaName(areaName);
            history.setCropName(name);

            if (uc.getCrop() != null) {
                Crop c = uc.getCrop();
                history.setVariety(c.getVariety());
                history.setWateringInterval(c.getWateringInterval());
                history.setFertilizingInterval(c.getFertilizingInterval());
                history.setSoilCareInterval(c.getSoilCareInterval());
                history.setProtectionInterval(c.getProtectionInterval());
            } else if (uc.getIndividualCrop() != null) {
                history.setVariety(uc.getIndividualCrop().getVariety());
                history.setWateringInterval(7); 
            }

            historyRepository.save(history);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GardenHistory>> getAllHistory(@PathVariable Integer userId) {
        List<GardenHistory> history = historyRepository.findAllByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/{userId}/planting")
    public ResponseEntity<List<GardenHistory>> getPlantingHistory(@PathVariable Integer userId) {
        // ИСПРАВЛЕНО: Ищем историю только конкретного пользователя, а не findAll() для всех
        List<GardenHistory> userHistory = historyRepository.findAllByUserId(userId);
        
        List<GardenHistory> plantingHistory = userHistory.stream()
                .filter(h -> h.getActionTypeId() != null && h.getActionTypeId() == 1)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(plantingHistory);
    }
}