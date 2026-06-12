package com.example.backend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GardenTaskController.class);

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
        log.info("========== getWeeklyTasks НАЧАЛО ==========");
        log.info("userId = {}", userId);
        
        List<Map<String, Object>> allTasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekLater = today.plusDays(7);
        
        log.info("today = {}, weekLater = {}", today, weekLater);
        
        Map<Integer, List<Weather>> weatherCache = loadWeatherCache(userId, today);
        log.info("weatherCache загружен, регионов: {}", weatherCache.size());
        
        List<GardenHistory> plantings = historyRepository.findAllPlantingsByUserId(userId);
        log.info("Найдено ЗАПИСЕЙ в garden_history (посадки): {}", plantings.size());
        
        for (GardenHistory plant : plantings) {
            log.info("  - cropName = '{}', areaName = '{}', variety = '{}', regionId = {}, userId = {}, doneAt = {}", 
                     plant.getCropName(), plant.getAreaName(), plant.getVariety(), 
                     plant.getRegionId(), plant.getUserId(), plant.getDoneAt());
        }
        
        List<UserCrop> futurePlantings = userCropRepository.findByUserIdAndPlantedAtAfter(userId, LocalDate.now());
        log.info("Найдено БУДУЩИХ посадок: {}", futurePlantings.size());
        
        for (UserCrop fp : futurePlantings) {
            log.info("  - futurePlant: cropId={}, individualCropId={}, plantedAt={}", 
                     fp.getCropId(), fp.getIndividualCropId(), fp.getPlantedAt());
        }
        
        List<Map<String, Object>> plannedTasks = getPlannedPlantingsInternal(userId, today);
        if (plannedTasks != null && !plannedTasks.isEmpty()) {
            allTasks.addAll(plannedTasks);
            log.info("Добавлено ЗАПЛАНИРОВАННЫХ посадок: {}", plannedTasks.size());
        }

        for (GardenHistory plant : plantings) {
            log.info("Обработка растения: {}", plant.getCropName());
            
            if (!shouldGenerateTasks(plant)) {
                log.info("  -> shouldGenerateTasks = false, пропускаем {}", plant.getCropName());
                continue;
            }
            
            log.info("  -> shouldGenerateTasks = true, генерируем задачи для {}", plant.getCropName());
            
            generateTasksForPlant(allTasks, plant, 
                                plant.getCropName(), 
                                plant.getVariety() != null ? plant.getVariety() : "Обычный",
                                plant.getAreaName(), 
                                plant.getGardenName(), 
                                plant.getRegionId(),
                                plant.getUserId(), 
                                plant.getDoneAt().toLocalDate(),
                                today, weekLater, weatherCache);
        }
        
        for (UserCrop futurePlant : futurePlantings) {
            LocalDate plantingDate = futurePlant.getPlantedAt();
            log.info("Обработка будущей посадки: plantedAt={}", plantingDate);
            if (plantingDate != null && !plantingDate.isAfter(weekLater)) {
                generateTasksForFuturePlant(allTasks, futurePlant, plantingDate, today, weekLater, weatherCache);
            }
        }

        log.info("Всего задач сгенерировано (до удаления дубликатов): {}", allTasks.size());
        
        List<Map<String, Object>> uniqueTasks = removeDuplicateTasksPerDay(allTasks);
        log.info("После удаления дубликатов: {}", uniqueTasks.size());
        
        uniqueTasks.sort(Comparator.comparing(t -> (String) t.get("dueDate")));
        
        List<Map<String, Object>> incompleteTasks = filterIncompleteTasks(uniqueTasks, userId);
        log.info("После фильтрации выполненных: {}", incompleteTasks.size());
        
        for (Map<String, Object> task : incompleteTasks) {
            log.info("  - ИТОГОВАЯ ЗАДАЧА: cropName={}, actionName={}, dueDate={}", 
                     task.get("cropName"), task.get("actionName"), task.get("dueDate"));
        }
        
        log.info("========== getWeeklyTasks КОНЕЦ ==========");
        return ResponseEntity.ok(incompleteTasks);
    }

    @GetMapping("/user/{userId}/planned-plantings")
    public ResponseEntity<List<Map<String, Object>>> getPlannedPlantings(@PathVariable Integer userId) {
            LocalDate today = LocalDate.now();
            List<Map<String, Object>> result = getPlannedPlantingsInternal(userId, today);
            return ResponseEntity.ok(result);
        }
        
        private List<Map<String, Object>> getPlannedPlantingsInternal(Integer userId, LocalDate today) {
        List<UserCrop> futureAndTodayPlantings = userCropRepository.findByUserIdAndPlantedAtGreaterThanEqual(userId, today);
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserCrop crop : futureAndTodayPlantings) {
            if (crop.getArea() == null) continue;
            
            // ПОЛУЧАЕМ НАЗВАНИЕ И СОРТ ИЗ СВЯЗАННЫХ ОБЪЕКТОВ
            String cropName = null;
            String variety = null;
            
            if (crop.getCrop() != null) {
                cropName = crop.getCrop().getName();
                variety = crop.getCrop().getVariety();
            } else if (crop.getIndividualCrop() != null) {
                cropName = crop.getIndividualCrop().getName();
                variety = crop.getIndividualCrop().getVariety();
            }
            
            if (cropName == null) continue; // пропускаем, если не можем определить название
            
            // Проверяем, нет ли уже записи в истории
            List<GardenHistory> existing = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    userId, 
                    cropName, 
                    crop.getArea().getName(), 
                    ActionTypeEnum.PLANTING.getId(),
                    crop.getPlantedAt().atStartOfDay(),
                    crop.getPlantedAt().atTime(LocalTime.MAX)
                );
            
            if (existing.isEmpty()) {
                Map<String, Object> task = new HashMap<>();
                task.put("cropName", cropName);
                task.put("variety", variety != null ? variety : "Обычный");
                task.put("areaName", crop.getArea().getName());
                task.put("gardenName", crop.getGarden() != null ? crop.getGarden().getName() : "");
                task.put("actionName", ActionTypeEnum.PLANTING.getDisplayName());
                task.put("actionTypeId", ActionTypeEnum.PLANTING.getId());
                task.put("dueDate", crop.getPlantedAt().toString());
                task.put("userCropId", crop.getId());
                task.put("isPlanned", true);
                task.put("isOverdue", crop.getPlantedAt().isBefore(today));
                result.add(task);
            }
        }
        
        return result;
    }
    @PostMapping("/user/{userId}/sync-past-plantings")
    public ResponseEntity<Map<String, Object>> syncPastPlantings(@PathVariable Integer userId) {
        LocalDate today = LocalDate.now();
        Map<String, Object> response = new HashMap<>();
        int createdCount = 0;
        
        List<UserCrop> pastPlantings = userCropRepository.findByUserIdAndPlantedAtLessThan(userId, today);
        
        for (UserCrop crop : pastPlantings) {
            if (crop.getArea() == null) continue;
            
            // ПОЛУЧАЕМ НАЗВАНИЕ И СОРТ
            String cropName = null;
            String variety = null;
            
            if (crop.getCrop() != null) {
                cropName = crop.getCrop().getName();
                variety = crop.getCrop().getVariety();
            } else if (crop.getIndividualCrop() != null) {
                cropName = crop.getIndividualCrop().getName();
                variety = crop.getIndividualCrop().getVariety();
            }
            
            if (cropName == null) continue;
            
            List<GardenHistory> existing = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    userId,
                    cropName,
                    crop.getArea().getName(),
                    ActionTypeEnum.PLANTING.getId(),
                    crop.getPlantedAt().atStartOfDay(),
                    crop.getPlantedAt().atTime(LocalTime.MAX)
                );
            
            if (existing.isEmpty()) {
                Integer regionId = crop.getArea() != null ? crop.getArea().getRegionId() : null;
                
                GardenHistory history = new GardenHistory();
                history.setUserId(userId);
                history.setActionTypeId(ActionTypeEnum.PLANTING.getId());
                history.setDoneAt(crop.getPlantedAt().atStartOfDay());
                history.setCropName(cropName);
                history.setVariety(variety != null ? variety : "Обычный");
                history.setAreaName(crop.getArea().getName());
                history.setGardenName(crop.getGarden() != null ? crop.getGarden().getName() : null);
                history.setRegionId(regionId);
                
                historyRepository.save(history);
                createdCount++;
                log.info("Автоматически создана запись посадки для {} на дату {}", cropName, crop.getPlantedAt());
            }
        }
        
        response.put("success", true);
        response.put("createdCount", createdCount);
        return ResponseEntity.ok(response);
    }

    private List<Map<String, Object>> filterIncompleteTasks(List<Map<String, Object>> tasks, Integer userId) {
        List<Map<String, Object>> incompleteTasks = new ArrayList<>();
        
        for (Map<String, Object> task : tasks) {
            String cropName = (String) task.get("cropName");
            String variety = (String) task.get("variety");
            String areaName = (String) task.get("areaName");
            Integer actionTypeId = (Integer) task.get("actionTypeId");
            String dueDateStr = (String) task.get("dueDate");
            Boolean isPlanned = (Boolean) task.get("isPlanned");
            
            // Для запланированных посадок проверяем отдельно
            if (isPlanned != null && isPlanned && actionTypeId == ActionTypeEnum.PLANTING.getId()) {
                // Они уже отфильтрованы при создании, просто добавляем
                incompleteTasks.add(task);
                continue;
            }
            
            if (dueDateStr == null) continue;
            
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            
            List<GardenHistory> existingTasks = historyRepository
                .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                    userId, cropName, areaName, actionTypeId,
                    dueDate.atStartOfDay(),
                    dueDate.atTime(LocalTime.MAX)
                );
            
            if (existingTasks.isEmpty()) {
                incompleteTasks.add(task);
            }
        }
        
        return incompleteTasks;
    }
    
    private Map<Integer, List<Weather>> loadWeatherCache(Integer userId, LocalDate today) {
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
        
        log.info("Регионы для загрузки погоды: {}", regionIds);
        
        for (Integer regionId : regionIds) {
            List<Weather> weather = weatherRepository.findByRegionIdAndDateGreaterThanEqual(regionId, today);
            if (weather != null && !weather.isEmpty()) {
                weatherCache.put(regionId, weather);
                log.info("Для региона {} загружено {} дней погоды", regionId, weather.size());
            } else {
                log.warn("Для региона {} погода НЕ загружена", regionId);
            }
        }
        
        return weatherCache;
    }
    
    private void generateTasksForPlant(List<Map<String, Object>> tasks, 
                                    GardenHistory plant,
                                    String cropName, 
                                    String variety,
                                    String areaName, 
                                    String gardenName,
                                    Integer regionId, 
                                    Integer userId, 
                                    LocalDate plantingDate,
                                    LocalDate today, 
                                    LocalDate weekLater,
                                    Map<Integer, List<Weather>> weatherCache) {
        
        log.info("===== generateTasksForPlant для: {} (variety={}) =====", cropName, variety);
        log.info("  plantingDate = {}, regionId = {}, userId = {}", plantingDate, regionId, userId);
        
        Integer wateringInterval = null;
        Integer fertilizingInterval = null;
        Integer soilCareInterval = null;
        Integer protectionInterval = null;
        Integer daysToHarvest = null;
        Integer daysToGermination = null;
        String finalVariety = variety;
        
        Optional<Crop> cropOpt = cropRepository.findByNameAndVariety(cropName, variety);
        
        if (cropOpt.isPresent()) {
            Crop crop = cropOpt.get();
            wateringInterval = crop.getWateringInterval();
            fertilizingInterval = crop.getFertilizingInterval();
            soilCareInterval = crop.getSoilCareInterval();
            protectionInterval = crop.getProtectionInterval();
            daysToHarvest = crop.getDaysToHarvest();
            daysToGermination = crop.getDaysToGermination();
            log.info("  Найдено в СИСТЕМНЫХ по имени+сорту: wateringInterval={}, fertilizingInterval={}, daysToHarvest={}", 
                    wateringInterval, fertilizingInterval, daysToHarvest);
        } else {
            log.info("  Не найдено в системных по имени+сорту, ищем в ПОЛЬЗОВАТЕЛЬСКИХ...");
            Optional<IndividualUserCrop> individualOpt = individualCropRepository
                .findByUserIdAndNameAndVariety(userId, cropName, variety);
            
            if (individualOpt.isPresent()) {
                IndividualUserCrop individual = individualOpt.get();
                wateringInterval = individual.getWateringInterval();
                fertilizingInterval = individual.getFertilizingInterval();
                soilCareInterval = individual.getSoilCareInterval();
                protectionInterval = individual.getProtectionInterval();
                daysToHarvest = individual.getDaysToHarvest();
                daysToGermination = individual.getDaysToGermination();
                finalVariety = individual.getVariety() != null ? individual.getVariety() : variety;
                log.info("  Найдено в ПОЛЬЗОВАТЕЛЬСКИХ по имени+сорту: wateringInterval={}, fertilizingInterval={}, daysToHarvest={}", 
                        wateringInterval, fertilizingInterval, daysToHarvest);
            } else {
                log.info("  Не найдено по имени+сорту, пробуем только по имени...");
                cropOpt = cropRepository.findByName(cropName);
                if (cropOpt.isPresent()) {
                    Crop crop = cropOpt.get();
                    wateringInterval = crop.getWateringInterval();
                    fertilizingInterval = crop.getFertilizingInterval();
                    soilCareInterval = crop.getSoilCareInterval();
                    protectionInterval = crop.getProtectionInterval();
                    daysToHarvest = crop.getDaysToHarvest();
                    daysToGermination = crop.getDaysToGermination();
                    log.info("  Найдено в СИСТЕМНЫХ по имени: wateringInterval={}", wateringInterval);
                } else {
                    Optional<IndividualUserCrop> individualOnlyOpt = individualCropRepository
                        .findByUserIdAndName(userId, cropName);
                    if (individualOnlyOpt.isPresent()) {
                        IndividualUserCrop individual = individualOnlyOpt.get();
                        wateringInterval = individual.getWateringInterval();
                        fertilizingInterval = individual.getFertilizingInterval();
                        soilCareInterval = individual.getSoilCareInterval();
                        protectionInterval = individual.getProtectionInterval();
                        daysToHarvest = individual.getDaysToHarvest();
                        daysToGermination = individual.getDaysToGermination();
                        finalVariety = individual.getVariety() != null ? individual.getVariety() : variety;
                        log.info("  Найдено в ПОЛЬЗОВАТЕЛЬСКИХ по имени: wateringInterval={}", wateringInterval);
                    } else {
                        log.warn("  НЕ НАЙДЕНО растение: cropName={}, variety={}, userId={}", cropName, variety, userId);
                        return;
                    }
                }
            }
        }
        
        if (wateringInterval != null && wateringInterval > 0) {
            log.info("  Генерация задач ПОЛИВА...");
            generateWateringTasksForPlant(tasks, plant, wateringInterval, daysToGermination,
                                        plantingDate, today, weekLater, weatherCache);
        }
        
        if (fertilizingInterval != null && fertilizingInterval > 0) {
            log.info("  Генерация задач УДОБРЕНИЯ...");
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.FERTILIZING, fertilizingInterval, finalVariety, today, weekLater, weatherCache);
        }
        
        if (soilCareInterval != null && soilCareInterval > 0) {
            log.info("  Генерация задач УХОД ЗА ПОЧВОЙ...");
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.SOIL_CARE, soilCareInterval, finalVariety, today, weekLater, weatherCache);
        }
        
        if (protectionInterval != null && protectionInterval > 0) {
            log.info("  Генерация задач ЗАЩИТА...");
            addTaskIfNotCompleted(tasks, plant, ActionTypeEnum.PROTECTION, protectionInterval, finalVariety, today, weekLater, weatherCache);
        }
        
        if (daysToHarvest != null && daysToHarvest > 0) {
            log.info("  Генерация задач СБОР УРОЖАЯ...");
            addHarvestTaskIfNotCompleted(tasks, plant, daysToHarvest, finalVariety, today, weekLater, weatherCache);
        }
    }
    
    private void generateWateringTasksForPlant(List<Map<String, Object>> tasks,
                                               GardenHistory plant,
                                               Integer wateringInterval,
                                               Integer daysToGermination,
                                               LocalDate plantingDate,
                                               LocalDate today,
                                               LocalDate weekLater,
                                               Map<Integer, List<Weather>> weatherCache) {
        
        if (wateringInterval == null || wateringInterval <= 0) return;
        
        int beforeGerminationInterval = wateringInterval * 2;
        
        List<GardenHistory> lastWaterings = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                plant.getVariety() != null ? plant.getVariety() : "Обычный",
                plant.getAreaName(),
                ActionTypeEnum.WATERING.getId()
            );
        
        LocalDate lastWateringDate = plantingDate;
        if (lastWaterings != null && !lastWaterings.isEmpty()) {
            lastWateringDate = lastWaterings.get(0).getDoneAt().toLocalDate();
            log.info("    Последний полив для {} был: {}", plant.getCropName(), lastWateringDate);
        } else {
            log.info("    Поливов для {} не было, начинаем с даты посадки: {}", plant.getCropName(), plantingDate);
        }
        
        LocalDate germinationEndDate = null;
        if (daysToGermination != null && daysToGermination > 0) {
            germinationEndDate = plantingDate.plusDays(daysToGermination);
            log.info("    Период до всходов до: {}", germinationEndDate);
        }
        
        LocalDate currentDate = lastWateringDate.plusDays(1);
        
        while (!currentDate.isAfter(weekLater)) {
            int currentInterval;
            
            if (germinationEndDate != null && currentDate.isBefore(germinationEndDate)) {
                currentInterval = beforeGerminationInterval;
            } else {
                currentInterval = wateringInterval;
            }
            
            long daysSinceLastWatering = java.time.temporal.ChronoUnit.DAYS.between(lastWateringDate, currentDate);
            if (daysSinceLastWatering >= currentInterval) {
                
                boolean shouldWater = checkWeatherForWatering(plant.getRegionId(), currentDate, weatherCache);
                
                if (shouldWater) {
                    List<GardenHistory> existingOnDate = historyRepository
                        .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                            plant.getUserId(),
                            plant.getCropName(),
                            plant.getAreaName(),
                            ActionTypeEnum.WATERING.getId(),
                            currentDate.atStartOfDay(),
                            currentDate.atTime(LocalTime.MAX)
                        );
                    
                    if (existingOnDate.isEmpty() && (currentDate.isEqual(today) || currentDate.isAfter(today))) {
                        Map<String, Object> task = buildTaskMap(plant, ActionTypeEnum.WATERING, currentDate, 
                                                                plant.getVariety() != null ? plant.getVariety() : "Обычный", today);
                        tasks.add(task);
                        log.info("    ДОБАВЛЕНА задача полива для {} на дату: {}", plant.getCropName(), currentDate);
                    }
                } else {
                    log.info("    Полив для {} на дату {} отменён из-за осадков", plant.getCropName(), currentDate);
                }
                
                lastWateringDate = currentDate;
            }
            
            currentDate = currentDate.plusDays(1);
        }
    }

    private void generateWateringTasksForFuturePlant(List<Map<String, Object>> tasks,
                                                    String cropName, String variety, String areaName, String gardenName,
                                                    Integer regionId, Integer userId,
                                                    Integer wateringInterval,
                                                    Integer daysToGermination,
                                                    LocalDate plantingDate,
                                                    LocalDate today, 
                                                    LocalDate weekLater,
                                                    Map<Integer, List<Weather>> weatherCache) {
        
        if (wateringInterval == null || wateringInterval <= 0) return;
        
        int beforeGerminationInterval = wateringInterval * 2;
        
        LocalDate currentDate = plantingDate.plusDays(1);
        LocalDate germinationEndDate = daysToGermination != null && daysToGermination > 0 
                                    ? plantingDate.plusDays(daysToGermination) 
                                    : null;
        
        while (!currentDate.isAfter(weekLater)) {
            int currentInterval;
            
            if (germinationEndDate != null && currentDate.isBefore(germinationEndDate)) {
                currentInterval = beforeGerminationInterval;
            } else {
                currentInterval = wateringInterval;
            }
            
            long daysSincePlanting = java.time.temporal.ChronoUnit.DAYS.between(plantingDate, currentDate);
            if (daysSincePlanting % currentInterval == 0) {
                
                boolean shouldWater = checkWeatherForWatering(regionId, currentDate, weatherCache);
                
                if (shouldWater) {
                    List<GardenHistory> existingOnDate = historyRepository
                        .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                            userId, cropName, areaName, ActionTypeEnum.WATERING.getId(),
                            currentDate.atStartOfDay(),
                            currentDate.atTime(LocalTime.MAX)
                        );
                    
                    if (existingOnDate.isEmpty() && (currentDate.isEqual(today) || currentDate.isAfter(today))) {
                        Map<String, Object> task = buildTaskMapSimple(cropName, variety, areaName, gardenName,
                                                                    ActionTypeEnum.WATERING, currentDate, today);
                        tasks.add(task);
                        log.info("    ДОБАВЛЕНА задача полива для {} на дату: {}", cropName, currentDate);
                    }
                } else {
                    log.info("    Полив для {} на дату {} отменён из-за осадков", cropName, currentDate);
                }
            }
            
            currentDate = currentDate.plusDays(1);
        }
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
        Integer daysToGermination = null;
        Integer regionId = null;
        
        if (futurePlant.getCrop() != null) {
            cropName = futurePlant.getCrop().getName();
            variety = futurePlant.getCrop().getVariety();
            
            Optional<Crop> cropOpt = cropRepository.findByNameAndVariety(cropName, variety);
            if (cropOpt.isPresent()) {
                Crop crop = cropOpt.get();
                wateringInterval = crop.getWateringInterval();
                fertilizingInterval = crop.getFertilizingInterval();
                soilCareInterval = crop.getSoilCareInterval();
                protectionInterval = crop.getProtectionInterval();
                daysToHarvest = crop.getDaysToHarvest();
                daysToGermination = crop.getDaysToGermination();
            } else {
                cropOpt = cropRepository.findByName(cropName);
                if (cropOpt.isPresent()) {
                    Crop crop = cropOpt.get();
                    wateringInterval = crop.getWateringInterval();
                    fertilizingInterval = crop.getFertilizingInterval();
                    soilCareInterval = crop.getSoilCareInterval();
                    protectionInterval = crop.getProtectionInterval();
                    daysToHarvest = crop.getDaysToHarvest();
                    daysToGermination = crop.getDaysToGermination();
                }
            }
        } else if (futurePlant.getIndividualCrop() != null) {
            cropName = futurePlant.getIndividualCrop().getName();
            variety = futurePlant.getIndividualCrop().getVariety();
            wateringInterval = futurePlant.getIndividualCrop().getWateringInterval();
            fertilizingInterval = futurePlant.getIndividualCrop().getFertilizingInterval();
            soilCareInterval = futurePlant.getIndividualCrop().getSoilCareInterval();
            protectionInterval = futurePlant.getIndividualCrop().getProtectionInterval();
            daysToHarvest = futurePlant.getIndividualCrop().getDaysToHarvest();
            daysToGermination = futurePlant.getIndividualCrop().getDaysToGermination();
        } else {
            return;
        }
        
        if (futurePlant.getArea() != null) {
            regionId = futurePlant.getArea().getRegionId();
        }
        
        String areaName = futurePlant.getArea() != null ? futurePlant.getArea().getName() : "Участок";
        String gardenName = futurePlant.getGarden() != null ? futurePlant.getGarden().getName() : null;
        Integer userId = futurePlant.getUserId();
        
        log.info("Будущая посадка: cropName={}, variety={}, plantingDate={}, regionId={}", cropName, variety, plantingDate, regionId);
        
        if (wateringInterval != null && wateringInterval > 0) {
            generateWateringTasksForFuturePlant(tasks, cropName, variety, areaName, gardenName, 
                                                regionId, userId, wateringInterval, daysToGermination,
                                                plantingDate, today, weekLater, weatherCache);
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

    private boolean shouldGenerateTasks(GardenHistory plant) {
        log.info("shouldGenerateTasks для: {}", plant.getCropName());
        
        List<GardenHistory> harvestHistory = historyRepository
            .findLastActionsByCropVarietyAreaAndType(
                plant.getCropName(),
                plant.getVariety() != null ? plant.getVariety() : "Обычный",
                plant.getAreaName(),
                ActionTypeEnum.HARVEST.getId()
            );
        
        if (!harvestHistory.isEmpty()) {
            log.info("  -> {} уже собран, пропускаем", plant.getCropName());
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
            
            log.info("  {}: plantingDate={}, harvestDate={}, harvestPeriodEnd={}, today={}", 
                     plant.getCropName(), plantingDate, harvestDate, harvestPeriodEnd, today);
            
            if (today.isAfter(harvestPeriodEnd)) {
                log.info("  -> период сбора урожая прошёл, пропускаем {}", plant.getCropName());
                return false;
            }
        }
        
        log.info("  -> возвращаем true для {}", plant.getCropName());
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
        
        log.info("    addTaskIfNotCompleted: {}, action={}, interval={}", plant.getCropName(), action.getDisplayName(), interval);
        
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
            log.info("      Последнее выполнение {} было: {}", action.getDisplayName(), lastDoneDate);
        } else {
            lastDoneDate = plant.getDoneAt().toLocalDate();
            log.info("      Выполнений {} не было, начинаем с даты посадки: {}", action.getDisplayName(), lastDoneDate);
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
                log.info("      ДОБАВЛЕНА задача {} для {} на дату: {}", action.getDisplayName(), plant.getCropName(), nextDueDate);
            } else {
                log.info("      Задача {} для {} на дату {} уже существует или выполнена", action.getDisplayName(), plant.getCropName(), nextDueDate);
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
                        log.info("      ДОБАВЛЕНА задача СБОРА УРОЖАЯ для {} на дату: {}", plant.getCropName(), currentDate);
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
                Float precipitation = w.getPrecipitation();
                if (precipitation != null && precipitation >= 5.0) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    private boolean checkWeatherForWatering(Integer regionId, LocalDate date, Map<Integer, List<Weather>> weatherCache) {
        if (regionId == null) return true;
        
        List<Weather> weatherList = weatherCache.get(regionId);
        if (weatherList == null || weatherList.isEmpty()) return true;
        
        for (Weather w : weatherList) {
            if (w.getDate() != null && w.getDate().equals(date)) {
                Float precipitation = w.getPrecipitation();
                if (precipitation != null && precipitation >= 5.0) {
                    return false;
                }
                return true;
            }
        }
        return true;
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
        task.put("isPlanned", false);
        
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
        task.put("variety", variety != null ? variety : "Обычный");
        task.put("areaName", areaName);
        task.put("gardenName", gardenName);
        task.put("actionName", action.getDisplayName()); 
        task.put("actionTypeId", action.getId());
        task.put("dueDate", dueDate.toString());
        task.put("isOverdue", dueDate.isBefore(today));
        task.put("lastDoneAt", null);
        task.put("isPlanned", false);
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
            Boolean isPlanned = (Boolean) request.get("isPlanned");
            Integer userCropId = (Integer) request.get("userCropId");
            
            if (dueDateStr == null) {
                response.put("success", false);
                response.put("error", "Не указана дата выполнения задачи");
                return ResponseEntity.badRequest().body(response);
            }
            
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            
            if (isPlanned != null && isPlanned && actionTypeId == ActionTypeEnum.PLANTING.getId() && userCropId != null) {
                return handlePlantedTask(response, userId, cropName, variety, areaName, gardenName, dueDate, userCropId);
            }
            
            if (dueDate.isAfter(LocalDate.now())) {
                response.put("success", false);
                response.put("error", "Эту задачу можно выполнить только в день выполнения");
                return ResponseEntity.badRequest().body(response);
            }
            
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
    
    private ResponseEntity<Map<String, Object>> handlePlantedTask(Map<String, Object> response,
                                                                Integer userId,
                                                                String cropName,
                                                                String variety,
                                                                String areaName,
                                                                String gardenName,
                                                                LocalDate dueDate,
                                                                Integer userCropId) {
        LocalDateTime startOfDueDate = dueDate.atStartOfDay();
        LocalDateTime endOfDueDate = dueDate.atTime(23, 59, 59);
        
        List<GardenHistory> existing = historyRepository
            .findByUserIdAndCropNameAndAreaNameAndActionTypeIdAndDoneAtBetween(
                userId, cropName, areaName, ActionTypeEnum.PLANTING.getId(), startOfDueDate, endOfDueDate
            );
        
        if (!existing.isEmpty()) {
            response.put("success", false);
            response.put("error", "Посадка уже выполнена");
            return ResponseEntity.badRequest().body(response);
        }
        
        Integer regionId = null;
        Optional<UserCrop> userCropOpt = userCropRepository.findById(userCropId);
        if (userCropOpt.isPresent()) {
            UserCrop uc = userCropOpt.get();
            if (uc.getArea() != null) {
                regionId = uc.getArea().getRegionId();
            }
            if (cropName == null) {
                if (uc.getCrop() != null) {
                    cropName = uc.getCrop().getName();
                } else if (uc.getIndividualCrop() != null) {
                    cropName = uc.getIndividualCrop().getName();
                }
            }
        }
        
        GardenHistory history = new GardenHistory();
        history.setUserId(userId);
        history.setActionTypeId(ActionTypeEnum.PLANTING.getId());
        history.setDoneAt(dueDate.atStartOfDay());
        history.setCropName(cropName);
        history.setVariety(variety != null ? variety : "Обычный");
        history.setAreaName(areaName);
        history.setGardenName(gardenName);
        history.setRegionId(regionId);
        
        historyRepository.save(history);
        
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}