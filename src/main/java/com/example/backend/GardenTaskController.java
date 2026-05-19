package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class GardenTaskController {

    @Autowired 
    private GardenHistoryRepository historyRepository;

    private enum ActionTypeEnum {
        PLANTING(1, "Посадка", null),
        WATERING(2, "Полив", GardenHistory::getWateringInterval),
        FERTILIZING(3, "Удобрение", GardenHistory::getFertilizingInterval),
        SOIL_CARE(4, "Уход за почвой", GardenHistory::getSoilCareInterval),
        PROTECTION(5, "Защитная обработка", GardenHistory::getProtectionInterval),
        HARVEST(6, "Сбор урожая", GardenHistory::getDaysToHarvest); 

        private final int id;
        private final String displayName;
        private final Function<GardenHistory, Integer> intervalExtractor;

        ActionTypeEnum(int id, String displayName, Function<GardenHistory, Integer> intervalExtractor) {
            this.id = id;
            this.displayName = displayName;
            this.intervalExtractor = intervalExtractor;
        }

        public int getId() { return id; }
        public String getDisplayName() { return displayName; }
        public Integer getInterval(GardenHistory plant) {
            return intervalExtractor != null ? intervalExtractor.apply(plant) : null;
        }
    }

    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTasks(@PathVariable Integer userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        List<GardenHistory> activePlantings = historyRepository.findAllPlantingsByUserId(userId);

        for (GardenHistory plant : activePlantings) {
            for (ActionTypeEnum action : ActionTypeEnum.values()) {
                if (action == ActionTypeEnum.PLANTING) continue;

                if (action == ActionTypeEnum.HARVEST) {
                    checkAndAddHarvestTask(tasks, plant, action, today, weekLater);
                } else {
                    checkAndAddCyclicTask(tasks, plant, action, today, weekLater);
                }
            }
        }

        tasks.sort(Comparator.comparing(t -> (String) t.get("dueDate")));
        return ResponseEntity.ok(tasks);
    }

    private void checkAndAddCyclicTask(List<Map<String, Object>> tasks, 
                                       GardenHistory plant, 
                                       ActionTypeEnum action, 
                                       LocalDate today, 
                                       LocalDate weekLater) {
        
        Integer interval = action.getInterval(plant);
        if (interval == null || interval <= 0) return;

        Optional<GardenHistory> lastAction = historyRepository
            .findTopByCropNameAndVarietyAndActionTypeIdOrderByDoneAtDesc(
                plant.getCropName(), 
                plant.getVariety(), 
                action.getId()
            );

        LocalDate lastDoneDate = lastAction
                .map(h -> h.getDoneAt().toLocalDate())
                .orElse(plant.getDoneAt().toLocalDate());

        LocalDate nextDueDate = lastDoneDate.plusDays(interval);

        if (!nextDueDate.isAfter(weekLater)) {
            tasks.add(buildTaskMap(plant, action, nextDueDate, today));
        }
    }

    private void checkAndAddHarvestTask(List<Map<String, Object>> tasks, 
                                        GardenHistory plant, 
                                        ActionTypeEnum action, 
                                        LocalDate today, 
                                        LocalDate weekLater) {
        
        Integer daysToHarvest = action.getInterval(plant);
        if (daysToHarvest == null || daysToHarvest <= 0) return;

        LocalDate plantingDate = plant.getDoneAt().toLocalDate();
        LocalDate harvestDate = plantingDate.plusDays(daysToHarvest);

        Optional<GardenHistory> alreadyHarvested = historyRepository
            .findTopByCropNameAndVarietyAndActionTypeIdOrderByDoneAtDesc(
                plant.getCropName(), 
                plant.getVariety(), 
                action.getId()
            );

        if (alreadyHarvested.isPresent()) return;

        if (!harvestDate.isAfter(weekLater)) {
            tasks.add(buildTaskMap(plant, action, harvestDate, today));
        }
    }


    private Map<String, Object> buildTaskMap(GardenHistory plant, ActionTypeEnum action, LocalDate dueDate, LocalDate today) {
        Map<String, Object> task = new HashMap<>();
        task.put("cropName", plant.getCropName());
        task.put("variety", plant.getVariety());
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
            history.setActionTypeId(actionTypeId);
            history.setDoneAt(LocalDateTime.now());
            history.setCropName(cropName);
            history.setVariety(variety != null ? variety : planting.get().getVariety());
            history.setAreaName(areaName);
            
            history.setWateringInterval(planting.get().getWateringInterval());
            history.setFertilizingInterval(planting.get().getFertilizingInterval());
            history.setSoilCareInterval(planting.get().getSoilCareInterval());
            history.setProtectionInterval(planting.get().getProtectionInterval());
            history.setDaysToHarvest(planting.get().getDaysToHarvest()); 
            
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