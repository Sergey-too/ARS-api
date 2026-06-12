package com.example.backend;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    
    @Autowired private UserCropRepository userCropRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private CropRepository cropRepository;
    @Autowired private IndividualUserCropRepository individualRepo;
    @Autowired private GardenHistoryRepository historyRepository;
    @Autowired private WeatherRepository weatherRepository;

    @PostMapping("/user/add")
    public ResponseEntity<Map<String, Object>> addUserCrop(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = (Integer) request.get("userId");
            Integer areaId = (Integer) request.get("areaId");
            Integer gardenId = (Integer) request.get("gardenId");
            Integer cropId = (Integer) request.get("cropId");
            Integer individualId = (Integer) request.get("individualCropId");
            
            String plantedAtStr = (String) request.get("plantedAt");
            String harvestedAtStr = (String) request.get("harvestedAt");

            // ========== 1. ПОЛУЧАЕМ ДАННЫЕ О РАСТЕНИИ ==========
            Integer daysToHarvest = null;
            String cropName = null;
            Integer minTemp = null;
            Integer maxTemp = null;
            Integer maxWind = null;
            
            if (cropId != null) {
                Optional<Crop> cropOpt = cropRepository.findById(cropId);
                if (cropOpt.isPresent()) {
                    Crop crop = cropOpt.get();
                    daysToHarvest = crop.getDaysToHarvest();
                    cropName = crop.getName();
                    if (crop.getMinTemp() != null) minTemp = crop.getMinTemp().intValue();
                    if (crop.getMaxTemp() != null) maxTemp = crop.getMaxTemp().intValue();
                    if (crop.getMaxWind() != null) maxWind = crop.getMaxWind().intValue();
                }
            } else if (individualId != null) {
                Optional<IndividualUserCrop> individualOpt = individualRepo.findById(individualId);
                if (individualOpt.isPresent()) {
                    IndividualUserCrop ind = individualOpt.get();
                    daysToHarvest = ind.getDaysToHarvest();
                    cropName = ind.getName();
                    if (ind.getMinTemp() != null) minTemp = ind.getMinTemp().intValue();
                    if (ind.getMaxTemp() != null) maxTemp = ind.getMaxTemp().intValue();
                    if (ind.getMaxWind() != null) maxWind = ind.getMaxWind().intValue();
                }
            } else {
                response.put("success", false);
                response.put("error", "Не указано растение");
                return ResponseEntity.badRequest().body(response);
            }

            if (cropName == null) {
                response.put("success", false);
                response.put("error", "Растение не найдено");
                return ResponseEntity.badRequest().body(response);
            }

            // ========== 2. ПРОВЕРКА ПОГОДЫ НА ДАТУ ПОСАДКИ ==========
            if (plantedAtStr != null && !plantedAtStr.isEmpty()) {
                LocalDate plantedAt = LocalDate.parse(plantedAtStr);
                
                Optional<Area> areaOpt = areaRepository.findById(areaId);
                if (areaOpt.isPresent() && areaOpt.get().getRegionId() != null) {
                    Integer regionId = areaOpt.get().getRegionId();
                    Weather weather = weatherRepository.findByRegionIdAndDate(regionId, plantedAt);
                    
                    if (weather != null) {
                        Short tempMinW = weather.getTemperatureMin();
                        Short tempMaxW = weather.getTemperatureMax();
                        Float precipitation = weather.getPrecipitation();
                        Short windMaxW = weather.getWindMax();
                        
                        if (minTemp != null && tempMinW != null && tempMinW < minTemp) {
                            response.put("success", false);
                            response.put("error", "Слишком холодно для посадки. Минимальная температура: " + minTemp + "°C");
                            return ResponseEntity.badRequest().body(response);
                        }
                        
                        if (maxTemp != null && tempMaxW != null && tempMaxW > maxTemp) {
                            response.put("success", false);
                            response.put("error", "Слишком жарко для посадки. Максимальная температура: " + maxTemp + "°C");
                            return ResponseEntity.badRequest().body(response);
                        }
                        
                        if (maxWind != null && windMaxW != null && windMaxW > maxWind) {
                            response.put("success", false);
                            response.put("error", "Слишком сильный ветер для посадки. Максимальная скорость: " + maxWind + " м/с");
                            return ResponseEntity.badRequest().body(response);
                        }
                        
                        if (precipitation != null && precipitation > 5.0) {
                            response.put("success", false);
                            response.put("error", "Из-за сильных осадков посадка не рекомендуется");
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                }
            }

            // ========== 3. ПРОВЕРКА ДАТЫ СБОРА ==========
            if (plantedAtStr != null && !plantedAtStr.isEmpty() && 
                harvestedAtStr != null && !harvestedAtStr.isEmpty()) {
                
                LocalDate plantedAt = LocalDate.parse(plantedAtStr);
                LocalDate harvestedAt = LocalDate.parse(harvestedAtStr);
                
                if (harvestedAt.isBefore(plantedAt)) {
                    response.put("success", false);
                    response.put("error", "Дата сбора не может быть раньше даты посадки");
                    return ResponseEntity.badRequest().body(response);
                }
                
                if (daysToHarvest != null && daysToHarvest > 0) {
                    LocalDate earliestHarvestDate = plantedAt.plusDays(daysToHarvest);
                    LocalDate latestHarvestDate = plantedAt.plusDays(daysToHarvest + 15);
                    
                    if (harvestedAt.isBefore(earliestHarvestDate)) {
                        response.put("success", false);
                        response.put("error", "Сбор урожая возможен не ранее " + earliestHarvestDate + 
                                " (через " + daysToHarvest + " дней после посадки)");
                        return ResponseEntity.badRequest().body(response);
                    }
                    
                    if (harvestedAt.isAfter(latestHarvestDate)) {
                        response.put("success", false);
                        response.put("error", "Сбор урожая должен быть не позднее " + latestHarvestDate + 
                                " (максимум " + (daysToHarvest + 15) + " дней после посадки)");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }

            // ========== 4. СОХРАНЕНИЕ ==========
            UserCrop userCrop = new UserCrop();
            userCrop.setUserId(userId);
            userCrop.setAreaId(areaId);
            userCrop.setGardenId(gardenId);

            if (plantedAtStr != null && !plantedAtStr.isEmpty()) {
                userCrop.setPlantedAt(LocalDate.parse(plantedAtStr));
            }
            if (harvestedAtStr != null && !harvestedAtStr.isEmpty()) {
                userCrop.setHarvestedAt(LocalDate.parse(harvestedAtStr));
            }

            if (cropId != null) {
                userCrop.setCropId(cropId);
            } else if (individualId != null) {
                userCrop.setIndividualCropId(individualId);
            }

            UserCrop saved = userCropRepository.save(userCrop);
            
            response.put("success", true);
            response.put("id", saved.getId());  
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserCrop>> getUserCrops(@PathVariable Integer userId) {
        try {
            List<UserCrop> userCrops = userCropRepository.findByUserIdWithDetails(userId);
            System.out.println("Найдено растений для пользователя " + userId + ": " + userCrops.size());
            return ResponseEntity.ok(userCrops);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/user/{userId}/{userCropId}")
    public ResponseEntity<Map<String, Object>> deleteUserCrop(@PathVariable Integer userId, @PathVariable Integer userCropId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<UserCrop> userCrop = userCropRepository.findById(userCropId);
            
            if (userCrop.isEmpty()) {
                response.put("success", false);
                response.put("error", "Растение не найдено");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!userCrop.get().getUserId().equals(userId)) {
                response.put("success", false);
                response.put("error", "Доступ запрещен");
                return ResponseEntity.status(403).body(response);
            }
            
            userCropRepository.delete(userCrop.get());
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

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

    @GetMapping("/user/{userId}/available")
    public ResponseEntity<List<UserCrop>> getAvailableToPlant(@PathVariable Integer userId) {
        try {
            List<UserCrop> allMyCrops = userCropRepository.findByUserIdWithDetails(userId);
            
            List<String> plantedNames = historyRepository.findAllPlantedCropNamesByUserId(userId);

            List<UserCrop> available = allMyCrops.stream()
                .filter(uc -> {
                    String name = (uc.getCrop() != null) ? uc.getCrop().getName() : 
                                (uc.getIndividualCrop() != null ? uc.getIndividualCrop().getName() : "");
                    return !plantedNames.contains(name);
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(available);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}