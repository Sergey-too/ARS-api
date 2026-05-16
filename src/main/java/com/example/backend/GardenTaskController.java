package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/tasks")
public class GardenTaskController {

    @Autowired 
    private GardenHistoryRepository historyRepository;

    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTasks(@PathVariable Integer userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);

        List<GardenHistory> activePlantings = historyRepository.findAllPlantingsByUserId(userId);

        for (GardenHistory plant : activePlantings) {
            
            checkAndAddTask(tasks, plant, 2, "Полив", plant.getWateringInterval(), today, weekLater);
            

            checkAndAddTask(tasks, plant, 3, "Удобрение", plant.getFertilizingInterval(), today, weekLater);
            

            checkAndAddTask(tasks, plant, 4, "Рыхление", plant.getSoilCareInterval(), today, weekLater);
            checkAndAddTask(tasks, plant, 5, "Защита", plant.getProtectionInterval(), today, weekLater);
        }

        tasks.sort(Comparator.comparing(t -> (String) t.get("dueDate")));

        return ResponseEntity.ok(tasks);
    }


    private void checkAndAddTask(List<Map<String, Object>> tasks, 
                                 GardenHistory plant, 
                                 int actionTypeId, 
                                 String actionName, 
                                 Integer interval, 
                                 LocalDate today, 
                                 LocalDate weekLater) {
        
        if (interval == null || interval <= 0) return;

        Optional<GardenHistory> lastAction = historyRepository
            .findTopByCropNameAndVarietyAndActionTypeIdOrderByDoneAtDesc(
                plant.getCropName(), 
                plant.getVariety(), 
                actionTypeId
            );

        LocalDate lastDoneDate = lastAction
                .map(h -> h.getDoneAt().toLocalDate())
                .orElse(plant.getDoneAt().toLocalDate());

        LocalDate nextDueDate = lastDoneDate.plusDays(interval);

        if (!nextDueDate.isAfter(weekLater)) {
            Map<String, Object> task = new HashMap<>();
            task.put("cropName", plant.getCropName());
            task.put("variety", plant.getVariety());
            task.put("areaName", plant.getAreaName());
            task.put("actionName", actionName);
            task.put("actionTypeId", actionTypeId);
            task.put("dueDate", nextDueDate.toString());

            task.put("isOverdue", nextDueDate.isBefore(today));
            
            tasks.add(task);
        }
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
                .findTopByCropNameAndActionTypeIdOrderByDoneAtDesc(cropName, 1);
            
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
            
            // Копируем интервалы из записи о посадке
            history.setWateringInterval(planting.get().getWateringInterval());
            history.setFertilizingInterval(planting.get().getFertilizingInterval());
            history.setSoilCareInterval(planting.get().getSoilCareInterval());
            history.setProtectionInterval(planting.get().getProtectionInterval());
            
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