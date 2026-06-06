package com.example.backend;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

            Integer userId = uc.getUserId(); 
            String areaName = (uc.getArea() != null) ? uc.getArea().getName() : "Участок";
            String gardenName = (uc.getGarden() != null) ? uc.getGarden().getName() : null;
            Integer regionId = (uc.getArea() != null && uc.getArea().getRegionId() != null) 
                    ? uc.getArea().getRegionId() : null;
            
            String name = (uc.getCrop() != null) ? uc.getCrop().getName() : 
                    (uc.getIndividualCrop() != null ? uc.getIndividualCrop().getName() : "Неизвестно");

            String variety = null;
            if (uc.getCrop() != null) {
                variety = uc.getCrop().getVariety();
            } else if (uc.getIndividualCrop() != null) {
                variety = uc.getIndividualCrop().getVariety();
            }

            boolean alreadyPlanted = historyRepository.existsByUserIdAndAreaNameAndCropNameAndVarietyAndActionTypeId(
                    userId, areaName, name, variety, 1
            );

            if (alreadyPlanted) {
                return ResponseEntity.badRequest().body(Map.of("error", "Это растение уже посажено на данном участке!"));
            }

            GardenHistory history = new GardenHistory();
            history.setUserId(userId);
            history.setActionTypeId(1); 
            history.setDoneAt(LocalDateTime.now());
            history.setAreaName(areaName);
            history.setGardenName(gardenName);
            history.setCropName(name);
            history.setVariety(variety);
            history.setRegionId(regionId);

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
        List<GardenHistory> userHistory = historyRepository.findAllByUserId(userId);
        
        List<GardenHistory> plantingHistory = userHistory.stream()
                .filter(h -> h.getActionTypeId() != null && h.getActionTypeId() == 1)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(plantingHistory);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addHistory(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = (Integer) request.get("userId");
            Integer actionTypeId = (Integer) request.get("actionTypeId");
            String doneAtStr = (String) request.get("doneAt");
            String cropName = (String) request.get("cropName");
            String variety = (String) request.get("variety");
            String areaName = (String) request.get("areaName");
            String gardenName = (String) request.get("gardenName");
            Integer regionId = (Integer) request.get("regionId");
            
            if (userId == null || actionTypeId == null || doneAtStr == null) {
                response.put("success", false);
                response.put("error", "Не все обязательные поля заполнены");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDateTime doneAt = LocalDateTime.parse(doneAtStr + "T00:00:00");
            
            GardenHistory history = new GardenHistory();
            history.setUserId(userId);
            history.setActionTypeId(actionTypeId);
            history.setDoneAt(doneAt);
            history.setCropName(cropName);
            history.setVariety(variety);
            history.setAreaName(areaName);
            history.setGardenName(gardenName);
            history.setRegionId(regionId);
            
            historyRepository.save(history);
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
}