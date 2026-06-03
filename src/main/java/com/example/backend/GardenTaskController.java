package com.example.backend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class GardenTaskController {

    @Autowired 
    private GardenHistoryRepository historyRepository;
    
    @Autowired
    private CropRepository cropRepository;

    @Autowired 
    private IndividualUserCropRepository individualCropRepository;
    
    @Autowired
    private UserCropRepository userCropRepository;
    
    @Autowired
    private WeatherRepository weatherRepository;

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

    private static final int HARVEST_PERIOD_DAYS = 15;

    @GetMapping("/user/{userId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getWeeklyTasks(@PathVariable Integer userId) {
        List<Map<String, Object>> allTasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<Integer, List<Weather>> weatherCache = new HashMap<>();
        Set<Integer> regionIds = new HashSet<>();
        
        List<GardenHistory> plantings = historyRepository.findAllPlantingsByUserId(userId);
        
        for (GardenHistory plant : plantings) {
            if (plant.getRegionId() != null) {
                regionIds.add(plant.getRegionId());
            }
        }
        
        List<UserCrop> futurePlantings = userCropRepository.findByUserIdAndPlantedAtAfter(userId, LocalDate.now());
        
        for (UserCrop futurePlant : futurePlantings) {
            if (futurePlant.getArea() != null && futurePlant.getArea().getRegionId() != null) {
                regionIds.add(futurePlant.getArea().getRegionId());
            }
        }
        
        for (Integer regionId : regionIds) {
            List<Weather> weather = weatherRepository.findByRegionIdAndDateGreaterThanEqual(regionId, today);
            if (weather != null && !weather.isEmpty()) {
                weatherCache.put(regionId, weather);
            }
        }
        
        for (GardenHistory plant : plantings) {
            if (!shouldGenerateTasks(plant)) {
                continue;
            }
            
            generateTasksForPlant(allTasks, plant, plant.getCropName(), plant.getVariety(), 
                                  plant.getAreaName(), plant.getGardenName(), plant.getRegionId(),
                                  plant.getUserId(), plant.getDoneAt().toLocalDate(),
                                  today, weekLater, weatherCache);
        }
        
        for (UserCrop futurePlant : futurePlantings) {
            LocalDate plantingDate = futurePlant.getPlantedAt();
            
            if (plantingDate != null && !plantingDate.isAfter(weekLater)) {
                generateTasksForFuturePlant(allTasks, futurePlant, plantingDate, today, weekLater, weatherCache);
            }
        }

        List<Map<String, Object>> uniqueTasks = removeDuplicateTasksPerDay(allTasks);
        uniqueTasks.sort(Comparator.comparing(t -> (String) t.get("dueDate")));
        return ResponseEntity.ok(uniqueTasks);
    }
    
    private void generateTasksForFuturePlant(List<Map<String, Object>> tasks, 
                                             UserCrop futurePlant,
                                             LocalDate plantingDate,
                                             LocalDate today, 
                                             LocalDate weekLater,
                                             Map<Integer, List<Weather>> weatherCache) {
        
        String cropName;
        String variety;
        Integer wateringInterval = null;
        Integer fertilizingInterval = null;
        Integer soilCareInterval = null;
        Integer protectionInterval = null;
        Integer daysToHarvest = null;
        Integer regionId = null;
        
        if (futurePlant.getCrop() != null) {
            cropName = futurePlant.getCrop().getName();
            variety = futurePlant.getCrop().getVariety();
            wateringInterval = futurePlant.getCrop().getWateringInterval();
            fertilizingInterval = futurePlant.getCrop().getFertilizingInterval();
            soilCareInterval = futurePlant.getCrop().getSoilCareInterval();
            protectionInterval = futurePlant.getCrop().getProtectionInterval();
            daysToHarvest = futurePlant.getCrop().getDaysToHarvest();
        } else if (futurePlant.getIndividualCrop() != null) {
            cropName = futurePlant.getIndividualCrop().getName();
            variety = futurePlant.getIndividualCrop().getVariety();
            wateringInterval = futurePlant.getIndividualCrop().getWateringInterval();
            fertilizingInterval = futurePlant.getIndividualCrop().getFertilizingInterval();
            soilCareInterval = futurePlant.getIndividualCrop().getSoilCareInterval();
            protectionInterval = futurePlant.getIndividualCrop().getProtectionInterval();
            daysToHarvest = futurePlant.getIndividualCrop().getDaysToHarvest();
        } else {
            return;
        }
        
        if (futurePlant.getArea() != null) {
            regionId = futurePlant.getArea().getRegionId();
        }
        
        String areaName = futurePlant.getArea() != null ? futurePlant.getArea().getName() : "Участок";
        String gardenName = futurePlant.getGarden() != null ? futurePlant.getGarden().getName() : null;
        Integer userId = futurePlant.getUserId();
        
        if (wateringInterval != null && wateringInterval > 0) {
            generateFutureTasks(tasks, cropName, variety, areaName, gardenName, regionId, userId,
                                ActionTypeEnum.WATERING, wateringInterval, plantingDate, today, weekLater, weatherCache);
        }
        if (fertilizingInterval != null && fertilizingInterval > 0) {
            generateFutureTasks(tasks, cropName, variety, areaName, gardenName, regionId, userId,
                                ActionTypeEnum.FERTILIZING, fertilizingInterval, plantingDate, today, weekLater, weatherCache);
        }
        if (soilCareInterval != null && soilCareInterval > 0) {
            generateFutureTasks(tasks, cropName, variety, areaName, gardenName, regionId, userId,
                                ActionTypeEnum.SOIL_CARE, soilCareInterval, plantingDate, today, weekLater, weatherCache);
        }
        if (protectionInterval != null && protectionInterval > 0) {
            generateFutureTasks(tasks, cropName, variety, areaName, gardenName, regionId, userId,
                                ActionTypeEnum.PROTECTION, protectionInterval, plantingDate, today, weekLater, weatherCache);
        }
        if (daysToHarvest != null && daysToHarvest > 0) {
            generateFutureHarvestTasks(tasks, cropName, variety, areaName, gardenName, regionId, userId,
                                       daysToHarvest, plantingDate, today, weekLater, weatherCache);
        }
    }
    
    private void generateFutureTasks(List<Map<String, Object>> tasks,
                                     String cropName, String variety, String areaName, String gardenName,
                                     Integer regionId, Integer userId,
                                     ActionTypeEnum action, Integer interval,
                                     LocalDate plantingDate, LocalDate today, LocalDate weekLater,
                                     Map<Integer, List<Weather>> weatherCache) {
        
        if (interval == null || interval <= 0) return;
        
        LocalDate nextDueDate = plantingDate.plusDays(interval);
        
        while (!nextDueDate.isAfter(weekLater)) {
            if (!hasWeatherForecast(regionId, nextDueDate, weatherCache)) {
                nextDueDate = nextDueDate.plusDays(interval);
                continue;
            }
            
            List<GardenHistory> existingOnDate = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    userId, cropName, areaName, action.getId(),
                    nextDueDate.atStartOfDay(),
                    nextDueDate.atTime(LocalTime.MAX)
                );
            
            if (existingOnDate.isEmpty() && (nextDueDate.isEqual(today) || nextDueDate.isAfter(today))) {
                tasks.add(buildTaskMapSimple(cropName, variety, areaName, gardenName, action, nextDueDate, today));
            }
            
            nextDueDate = nextDueDate.plusDays(interval);
        }
    }
    
    private void generateFutureHarvestTasks(List<Map<String, Object>> tasks,
                                            String cropName, String variety, String areaName, String gardenName,
                                            Integer regionId, Integer userId,
                                            Integer daysToHarvest,
                                            LocalDate plantingDate, LocalDate today, LocalDate weekLater,
                                            Map<Integer, List<Weather>> weatherCache) {
        
        if (daysToHarvest == null || daysToHarvest <= 0) return;
        
        LocalDate harvestDate = plantingDate.plusDays(daysToHarvest);
        LocalDate harvestPeriodEnd = harvestDate.plusDays(HARVEST_PERIOD_DAYS);
        
        if (today.isAfter(harvestPeriodEnd)) {
            return;
        }
        
        LocalDate currentDate = harvestDate;
        while (!currentDate.isAfter(harvestPeriodEnd) && !currentDate.isAfter(weekLater)) {
            if (hasWeatherForecast(regionId, currentDate, weatherCache)) {
                if (currentDate.isEqual(today) || currentDate.isAfter(today)) {
                    List<GardenHistory> existingOnDate = historyRepository
                        .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                            userId, cropName, areaName, ActionTypeEnum.HARVEST.getId(),
                            currentDate.atStartOfDay(),
                            currentDate.atTime(LocalTime.MAX)
                        );
                    
                    if (existingOnDate.isEmpty()) {
                        tasks.add(buildTaskMapSimple(cropName, variety, areaName, gardenName, 
                                                     ActionTypeEnum.HARVEST, currentDate, today));
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }
    
    private void generateTasksForPlant(List<Map<String, Object>> tasks, 
                                   GardenHistory plant,
                                   String cropName, String variety, String areaName, String gardenName,
                                   Integer regionId, Integer userId, LocalDate plantingDate,
                                   LocalDate today, LocalDate weekLater,
                                   Map<Integer, List<Weather>> weatherCache) {
        
        Optional<Crop> cropOpt = cropRepository.findByName(cropName);
        
        Integer wateringInterval = null;
        Integer fertilizingInterval = null;
        Integer soilCareInterval = null;
        Integer protectionInterval = null;
        Integer daysToHarvest = null;
        
        if (cropOpt.isPresent()) {
            Crop crop = cropOpt.get();
            wateringInterval = crop.getWateringInterval();
            fertilizingInterval = crop.getFertilizingInterval();
            soilCareInterval = crop.getSoilCareInterval();
            protectionInterval = crop.getProtectionInterval();
            daysToHarvest = crop.getDaysToHarvest();
        } else {
            Optional<IndividualUserCrop> individualOpt = individualCropRepository
                .findByUserIdAndName(userId, cropName);
            
            if (individualOpt.isPresent()) {
                IndividualUserCrop individual = individualOpt.get();
                wateringInterval = individual.getWateringInterval();
                fertilizingInterval = individual.getFertilizingInterval();
                soilCareInterval = individual.getSoilCareInterval();
                protectionInterval = individual.getProtectionInterval();
                daysToHarvest = individual.getDaysToHarvest();
                variety = individual.getVariety() != null ? individual.getVariety() : "Обычный";
            }
        }
        
        if (wateringInterval != null && wateringInterval > 0) {
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.WATERING, wateringInterval, variety, today, weekLater, weatherCache);
        }
        if (fertilizingInterval != null && fertilizingInterval > 0) {
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.FERTILIZING, fertilizingInterval, variety, today, weekLater, weatherCache);
        }
        if (soilCareInterval != null && soilCareInterval > 0) {
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.SOIL_CARE, soilCareInterval, variety, today, weekLater, weatherCache);
        }
        if (protectionInterval != null && protectionInterval > 0) {
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.PROTECTION, protectionInterval, variety, today, weekLater, weatherCache);
        }
        if (daysToHarvest != null && daysToHarvest > 0) {
            addHarvestTaskIfNotCompleted(tasks, plant, daysToHarvest, variety, today, weekLater, weatherCache);
        }
    }

    private boolean shouldGenerateTasks(GardenHistory plant) {
        List<GardenHistory> harvestHistory = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                plant.getVariety() != null ? plant.getVariety() : "Обычный",
                plant.getAreaName(),
                ActionTypeEnum.HARVEST.getId()
            );
        
        if (!harvestHistory.isEmpty()) {
            return false;
        }
        
        Optional<Crop> cropOpt = cropRepository.findByName(plant.getCropName());
        Integer daysToHarvest = null;
        
        if (cropOpt.isPresent()) {
            daysToHarvest = cropOpt.get().getDaysToHarvest();
        } else {
            Optional<IndividualUserCrop> individualOpt = individualCropRepository
                .findByUserIdAndName(plant.getUserId(), plant.getCropName());
            if (individualOpt.isPresent()) {
                daysToHarvest = individualOpt.get().getDaysToHarvest();
            }
        }
        
        if (daysToHarvest != null && daysToHarvest > 0) {
            LocalDate plantingDate = plant.getDoneAt().toLocalDate();
            LocalDate harvestDate = plantingDate.plusDays(daysToHarvest);
            LocalDate today = LocalDate.now();
            LocalDate harvestPeriodEnd = harvestDate.plusDays(HARVEST_PERIOD_DAYS);
            
            if (today.isAfter(harvestPeriodEnd)) {
                return false;
            }
        }
        
        return true;
    }

    private void addTaskIfNotCompleted(List<Map<String, Object>> tasks, 
                                       GardenHistory plant,
                                       ActionTypeEnum action, 
                                       Integer interval,
                                       String variety,
                                       LocalDate today, 
                                       LocalDate weekLater,
                                       Map<Integer, List<Weather>> weatherCache) {
        
        if (interval == null || interval <= 0) return;

        List<GardenHistory> lastActions = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                variety,
                plant.getAreaName(),
                action.getId()
            );

        LocalDate lastDoneDate;
        if (lastActions != null && !lastActions.isEmpty()) {
            lastDoneDate = lastActions.get(0).getDoneAt().toLocalDate();
        } else {
            lastDoneDate = plant.getDoneAt().toLocalDate();
        }

        LocalDate nextDueDate = lastDoneDate.plusDays(interval);
        
        while (!nextDueDate.isAfter(weekLater)) {
            if (!hasWeatherForecast(plant.getRegionId(), nextDueDate, weatherCache)) {
                nextDueDate = nextDueDate.plusDays(interval);
                continue;
            }
            
            List<GardenHistory> existingOnDate = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    plant.getUserId(),
                    plant.getCropName(),
                    plant.getAreaName(),
                    action.getId(),
                    nextDueDate.atStartOfDay(),
                    nextDueDate.atTime(LocalTime.MAX)
                );
            
            if (existingOnDate.isEmpty() && (nextDueDate.isEqual(today) || nextDueDate.isAfter(today))) {
                tasks.add(buildTaskMap(plant, action, nextDueDate, variety, today));
            }
            
            nextDueDate = nextDueDate.plusDays(interval);
        }
    }

    private void addHarvestTaskIfNotCompleted(List<Map<String, Object>> tasks, 
                                              GardenHistory plant,
                                              Integer daysToHarvest,
                                              String variety,
                                              LocalDate today, 
                                              LocalDate weekLater,
                                              Map<Integer, List<Weather>> weatherCache) {
        
        if (daysToHarvest == null || daysToHarvest <= 0) return;

        LocalDate plantingDate = plant.getDoneAt().toLocalDate();
        LocalDate harvestDate = plantingDate.plusDays(daysToHarvest);
        LocalDate harvestPeriodEnd = harvestDate.plusDays(HARVEST_PERIOD_DAYS);

        if (today.isAfter(harvestPeriodEnd)) {
            return;
        }

        List<GardenHistory> alreadyHarvested = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                variety,
                plant.getAreaName(),
                ActionTypeEnum.HARVEST.getId()
            );

        if (!alreadyHarvested.isEmpty()) return;

        LocalDate currentDate = harvestDate;
        while (!currentDate.isAfter(harvestPeriodEnd) && !currentDate.isAfter(weekLater)) {
            if (hasWeatherForecast(plant.getRegionId(), currentDate, weatherCache)) {
                if (currentDate.isEqual(today) || currentDate.isAfter(today)) {
                    List<GardenHistory> existingOnDate = historyRepository
                        .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                            plant.getUserId(),
                            plant.getCropName(),
                            plant.getAreaName(),
                            ActionTypeEnum.HARVEST.getId(),
                            currentDate.atStartOfDay(),
                            currentDate.atTime(LocalTime.MAX)
                        );
                    
                    if (existingOnDate.isEmpty()) {
                        tasks.add(buildTaskMap(plant, ActionTypeEnum.HARVEST, currentDate, variety, today));
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    private boolean hasWeatherForecast(Integer regionId, LocalDate date, Map<Integer, List<Weather>> weatherCache) {
        if (regionId == null) return true;
        
        List<Weather> weatherList = weatherCache.get(regionId);
        if (weatherList == null || weatherList.isEmpty()) return false;
        
        for (Weather w : weatherList) {
            if (w.getDate() != null && w.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, Object>> removeDuplicateTasksPerDay(List<Map<String, Object>> tasks) {
        Map<String, Map<String, Object>> uniqueTasks = new LinkedHashMap<>();
        
        for (Map<String, Object> task : tasks) {
            String cropName = (String) task.get("cropName");
            String areaName = (String) task.get("areaName");
            String dueDate = (String) task.get("dueDate");
            Integer actionTypeId = (Integer) task.get("actionTypeId");
            
            String key = cropName + "|" + areaName + "|" + dueDate + "|" + actionTypeId;
            
            if (!uniqueTasks.containsKey(key)) {
                uniqueTasks.put(key, task);
            }
        }
        
        return new ArrayList<>(uniqueTasks.values());
    }

    private Map<String, Object> buildTaskMap(GardenHistory plant, 
                                             ActionTypeEnum action, 
                                             LocalDate dueDate, 
                                             String variety, 
                                             LocalDate today) {
        Map<String, Object> task = new HashMap<>();
        task.put("cropName", plant.getCropName());
        task.put("variety", variety);
        task.put("areaName", plant.getAreaName());
        task.put("gardenName", plant.getGardenName());
        task.put("actionName", action.getDisplayName()); 
        task.put("actionTypeId", action.getId());
        task.put("dueDate", dueDate.toString());
        task.put("isOverdue", dueDate.isBefore(today));
        
        List<GardenHistory> lastActions = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                variety,
                plant.getAreaName(),
                action.getId()
            );
        
        String lastDoneAt = null;
        if (lastActions != null && !lastActions.isEmpty()) {
            lastDoneAt = lastActions.get(0).getDoneAt().toLocalDate().toString();
        }
        task.put("lastDoneAt", lastDoneAt);
        
        return task;
    }
    
    private Map<String, Object> buildTaskMapSimple(String cropName, String variety, String areaName, String gardenName,
                                                   ActionTypeEnum action, LocalDate dueDate, LocalDate today) {
        Map<String, Object> task = new HashMap<>();
        task.put("cropName", cropName);
        task.put("variety", variety);
        task.put("areaName", areaName);
        task.put("gardenName", gardenName);
        task.put("actionName", action.getDisplayName()); 
        task.put("actionTypeId", action.getId());
        task.put("dueDate", dueDate.toString());
        task.put("isOverdue", dueDate.isBefore(today));
        task.put("lastDoneAt", null);
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
            String gardenName = (String) request.get("gardenName");
            Integer userId = (Integer) request.get("userId");
            String dueDateStr = (String) request.get("dueDate");
            
            if (dueDateStr == null) {
                response.put("success", false);
                response.put("error", "Не указана дата выполнения задачи");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            
            LocalDateTime startOfDueDate = dueDate.atStartOfDay();
            LocalDateTime endOfDueDate = dueDate.atTime(23, 59, 59);
            
            List<GardenHistory> existingTasksOnDate = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    userId, cropName, areaName, actionTypeId, startOfDueDate, endOfDueDate
                );
            
            if (!existingTasksOnDate.isEmpty()) {
                response.put("success", false);
                response.put("error", "Задача на " + dueDate + " уже выполнена");
                return ResponseEntity.badRequest().body(response);
            }
            
            Optional<GardenHistory> planting = historyRepository
                .findPlantingByCropVarietyArea(cropName, variety, areaName);
            
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
            history.setVariety(variety);
            history.setAreaName(areaName);
            history.setGardenName(gardenName);
            history.setRegionId(planting.get().getRegionId());
            
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