package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class GardenTaskController {

    @Autowired 
    private GardenHistoryRepository historyRepository;
    
    @Autowired
    private CropRepository cropRepository;

    private enum ActionTypeEnum {
        PLANTING(1, "Посадка"),
        WATERING(2, "Полив"),
        FERTILIZING(3, "Удобрение"),
        SOIL_CARE(4, "Уход за почвой"),
        PROTECTION(5, "Защитная обработка"),
        HARVEST(6, "Сбор урожая"); 

        private final int id;
        private final String displayName;

        ActionTypeEnum(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public int getId() { return id; }
        public String getDisplayName() { return displayName; }
    }

    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTasks(@PathVariable Integer userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        List<GardenHistory> activePlantings = historyRepository.findAllPlantingsByUserId(userId);

        for (GardenHistory plant : activePlantings) {
            Optional<Crop> cropOpt = cropRepository.findByName(plant.getCropName());
            if (cropOpt.isEmpty()) continue;
            
            Crop crop = cropOpt.get();
            
            checkAndAddCyclicTask(tasks, plant, crop, ActionTypeEnum.WATERING, crop.getWateringInterval(), today, weekLater);
            checkAndAddCyclicTask(tasks, plant, crop, ActionTypeEnum.FERTILIZING, crop.getFertilizingInterval(), today, weekLater);
            checkAndAddCyclicTask(tasks, plant, crop, ActionTypeEnum.SOIL_CARE, crop.getSoilCareInterval(), today, weekLater);
            checkAndAddCyclicTask(tasks, plant, crop, ActionTypeEnum.PROTECTION, crop.getProtectionInterval(), today, weekLater);
            checkAndAddHarvestTask(tasks, plant, crop, crop.getDaysToHarvest(), today, weekLater);
        }

        tasks.sort(Comparator.comparing(t -> (String) t.get("dueDate")));
        return ResponseEntity.ok(tasks);
    }

    private void checkAndAddCyclicTask(List<Map<String, Object>> tasks, 
                                       GardenHistory plant,
                                       Crop crop,
                                       ActionTypeEnum action, 
                                       Integer interval,
                                       LocalDate today, 
                                       LocalDate weekLater) {
        
        if (interval == null || interval <= 0) return;

        Optional<GardenHistory> lastAction = historyRepository
            .findTopByCropNameAndActionTypeIdOrderByDoneAtDesc(
                plant.getCropName(), 
                action.getId()
            );

        LocalDate lastDoneDate = lastAction
                .map(h -> h.getDoneAt().toLocalDate())
                .orElse(plant.getDoneAt().toLocalDate());

        LocalDate nextDueDate = lastDoneDate.plusDays(interval);

        if (!nextDueDate.isAfter(weekLater)) {
            tasks.add(buildTaskMap(plant, crop, action, nextDueDate, today));
        }
    }

    private void checkAndAddHarvestTask(List<Map<String, Object>> tasks, 
                                        GardenHistory plant,
                                        Crop crop,
                                        Integer daysToHarvest,
                                        LocalDate today, 
                                        LocalDate weekLater) {
        
        if (daysToHarvest == null || daysToHarvest <= 0) return;

        LocalDate plantingDate = plant.getDoneAt().toLocalDate();
        LocalDate harvestDate = plantingDate.plusDays(daysToHarvest);

        Optional<GardenHistory> alreadyHarvested = historyRepository
            .findTopByCropNameAndActionTypeIdOrderByDoneAtDesc(
                plant.getCropName(), 
                ActionTypeEnum.HARVEST.getId()
            );

        if (alreadyHarvested.isPresent()) return;

        if (!harvestDate.isAfter(weekLater)) {
            tasks.add(buildTaskMap(plant, crop, ActionTypeEnum.HARVEST, harvestDate, today));
        }
    }

    private Map<String, Object> buildTaskMap(GardenHistory plant, Crop crop, ActionTypeEnum action, LocalDate dueDate, LocalDate today) {
        Map<String, Object> task = new HashMap<>();
        task.put("cropName", plant.getCropName());
        task.put("variety", crop.getVariety() != null ? crop.getVariety() : "Обычный");
        task.put("areaName", plant.getAreaName());
        task.put("actionName", action.getDisplayName()); 
        task.put("actionTypeId", action.getId());
        task.put("dueDate", dueDate.toString());
        task.put("isOverdue", dueDate.isBefore(today));
        return task;
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeTask(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String cropName = (String) request.get("cropName");
            String variety = (String) request.get("variety");
            String areaName = (String) request.get("areaName");
            Integer actionTypeId = (Integer) request.get("actionTypeId");
     
            Optional<GardenHistory> planting = historyRepository
                .findTopByCropNameAndActionTypeIdOrderByDoneAtDesc(cropName, ActionTypeEnum.PLANTING.getId());
            
            if (planting.isEmpty()) {
                response.put("success", false);
                response.put("error", "Растение не найдено в истории посадок");
                return ResponseEntity.badRequest().body(response);
            }
            
            GardenHistory history = new GardenHistory();
            history.setUserId(planting.get().getUserId());
            history.setActionTypeId(actionTypeId);
            history.setDoneAt(LocalDateTime.now());
            history.setCropName(cropName);
            history.setVariety(variety != null ? variety : planting.get().getVariety());
            history.setAreaName(areaName);
            
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