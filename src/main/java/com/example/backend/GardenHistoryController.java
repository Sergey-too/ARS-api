package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class GardenHistoryController {

    @Autowired private GardenHistoryRepository historyRepository;
    @Autowired private UserCropRepository userCropRepository;

    @PostMapping("/plant") 
    public ResponseEntity<?> plantCrop(@RequestBody Map<String, Object> request) {
        try {
            Integer userCropId = (Integer) request.get("userCropId");
            
            // Используем детальную подгрузку, чтобы crop и individualCrop не были null
            UserCrop uc = userCropRepository.findById(userCropId)
                    .orElseThrow(() -> new RuntimeException("Культура не найдена"));

            String name = (uc.getCrop() != null) ? uc.getCrop().getName() : 
                        (uc.getIndividualCrop() != null ? uc.getIndividualCrop().getName() : "Неизвестно");

            // БЛОКИРОВКА ДУБЛЕЙ
            if (historyRepository.existsByCropNameAndActionTypeId(name, 1)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Это растение уже посажено!"));
            }

            GardenHistory history = new GardenHistory();
            history.setActionTypeId(1); 
            history.setDoneAt(LocalDateTime.now());
            history.setAreaName(uc.getArea() != null ? uc.getArea().getName() : "Участок");
            history.setCropName(name);

            // ЖЕСТКОЕ КОПИРОВАНИЕ ИНТЕРВАЛОВ
            if (uc.getCrop() != null) {
                Crop c = uc.getCrop();
                history.setVariety(c.getVariety());
                history.setWateringInterval(c.getWateringInterval());
                history.setFertilizingInterval(c.getFertilizingInterval());
                history.setSoilCareInterval(c.getSoilCareInterval());
                history.setProtectionInterval(c.getProtectionInterval());
            } else if (uc.getIndividualCrop() != null) {
                history.setVariety(uc.getIndividualCrop().getVariety());
                // Для личных ставим дефолт или 0
                history.setWateringInterval(7); 
            }

            historyRepository.save(history);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}