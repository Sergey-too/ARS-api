package com.example.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CropController {
    
    @Autowired
    private CompatibilityRepository compatibilityRepository; 

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CropRepository cropRepository;
    
    @Autowired
    private UserCropRepository userCropRepository;

    @GetMapping("/crops/by-category/{categoryName}")
    public ResponseEntity<List<Crop>> getCropsByCategory(@PathVariable String categoryName) {
        List<Crop> crops = cropRepository.findByCategoryName(categoryName);
        return ResponseEntity.ok(crops);
    }

    @PostMapping("/crops")
    public ResponseEntity<Crop> addCrop(@RequestBody CropRequest cropRequest) {
        try {
            Crop crop = new Crop();
            fillCropData(crop, cropRequest);

            if (cropRequest.getCategory() != null && !cropRequest.getCategory().isEmpty()) {
                Category category = categoryRepository.findByName(cropRequest.getCategory());
                if (category != null) {
                    crop.setCategory(category);
                } else {
                    Category newCategory = new Category();
                    newCategory.setName(cropRequest.getCategory());
                    newCategory = categoryRepository.save(newCategory);
                    crop.setCategory(newCategory);
                }
            }
            
            Crop savedCrop = cropRepository.save(crop);
            return ResponseEntity.ok(savedCrop);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/crops")
    public ResponseEntity<List<Crop>> getAllCrops() {
        return ResponseEntity.ok(cropRepository.findAll());
    }
    
    @GetMapping("/crops/{id}")
    public ResponseEntity<Crop> getCropById(@PathVariable Integer id) {
        return cropRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/crops/{id}")
    public ResponseEntity<Crop> updateCrop(@PathVariable Integer id, @RequestBody CropRequest cropRequest) {
        try {
            Crop existingCrop = cropRepository.findById(id).orElse(null);
            if (existingCrop == null) return ResponseEntity.notFound().build();
            
            fillCropData(existingCrop, cropRequest);
            
            if (cropRequest.getCategory() != null && !cropRequest.getCategory().equals(existingCrop.getCategoryName())) {
                Category category = categoryRepository.findByName(cropRequest.getCategory());
                if (category != null) {
                    existingCrop.setCategory(category);
                } else {
                    Category newCategory = new Category();
                    newCategory.setName(cropRequest.getCategory());
                    newCategory = categoryRepository.save(newCategory);
                    existingCrop.setCategory(newCategory);
                }
            }
            
            return ResponseEntity.ok(cropRepository.save(existingCrop));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void fillCropData(Crop crop, CropRequest req) {
        crop.setName(req.getName());
        crop.setDescription(req.getDescription());
        crop.setMinTemp(req.getMinTemp());
        crop.setMaxTemp(req.getMaxTemp());
        crop.setMaxWind(req.getMaxWind());
        crop.setMinHumidity(req.getMinHumidity());
        crop.setMaxHumidity(req.getMaxHumidity());
        crop.setNeededPrecipitation(req.getNeededPrecipitation());
        crop.setSowingDepth(req.getSowingDepth());
        crop.setDaysToGermination(req.getDaysToGermination());
        crop.setDaysToHarvest(req.getDaysToHarvest());
        crop.setCanSeedlings(req.getCanSeedlings());
        crop.setCanDirectSow(req.getCanDirectSow());
        crop.setPhotoPath(req.getPhotoPath());
        crop.setVariety(req.getVariety());
        crop.setWateringInterval(req.getWateringInterval());
        crop.setFertilizingInterval(req.getFertilizingInterval());
        crop.setSoilCareInterval(req.getSoilCareInterval());
        crop.setProtectionInterval(req.getProtectionInterval());
    }
    
    @DeleteMapping("/crops/{id}")
    public ResponseEntity<Map<String, Object>> deleteCrop(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!cropRepository.existsById(id)) {
                response.put("success", false);
                return ResponseEntity.status(404).body(response);
            }
            
            compatibilityRepository.deleteByCropId(id);
            
            List<UserCrop> userCrops = userCropRepository.findByCropId(id);
            if (userCrops != null && !userCrops.isEmpty()) {
                userCropRepository.deleteAll(userCrops);
            }

            cropRepository.deleteById(id);
            
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public static class CropRequest {
        private String name;
        private String category;
        private String description;
        private Float minTemp;
        private Float maxTemp;
        private Float maxWind;
        private Integer minHumidity;
        private Integer maxHumidity;
        private Float neededPrecipitation;
        private Integer sowingDepth;
        private Integer daysToGermination;
        private Integer daysToHarvest;
        private Boolean canSeedlings;
        private Boolean canDirectSow;
        private String photoPath;
        
        private String variety;
        private Integer wateringInterval;
        private Integer fertilizingInterval;
        private Integer soilCareInterval;
        private Integer protectionInterval;

        public String getVariety() { return variety; }
        public void setVariety(String variety) { this.variety = variety; }

        public Integer getWateringInterval() { return wateringInterval; }
        public void setWateringInterval(Integer wateringInterval) { this.wateringInterval = wateringInterval; }

        public Integer getFertilizingInterval() { return fertilizingInterval; }
        public void setFertilizingInterval(Integer fertilizingInterval) { this.fertilizingInterval = fertilizingInterval; }

        public Integer getSoilCareInterval() { return soilCareInterval; }
        public void setSoilCareInterval(Integer soilCareInterval) { this.soilCareInterval = soilCareInterval; }

        public Integer getProtectionInterval() { return protectionInterval; }
        public void setProtectionInterval(Integer protectionInterval) { this.protectionInterval = protectionInterval; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Float getMinTemp() { return minTemp; }
        public void setMinTemp(Float minTemp) { this.minTemp = minTemp; }
        public Float getMaxTemp() { return maxTemp; }
        public void setMaxTemp(Float maxTemp) { this.maxTemp = maxTemp; }
        public Float getMaxWind() { return maxWind; }
        public void setMaxWind(Float maxWind) { this.maxWind = maxWind; }
        public Integer getMinHumidity() { return minHumidity; }
        public void setMinHumidity(Integer minHumidity) { this.minHumidity = minHumidity; }
        public Integer getMaxHumidity() { return maxHumidity; }
        public void setMaxHumidity(Integer maxHumidity) { this.maxHumidity = maxHumidity; }
        public Float getNeededPrecipitation() { return neededPrecipitation; }
        public void setNeededPrecipitation(Float neededPrecipitation) { this.neededPrecipitation = neededPrecipitation; }
        public Integer getSowingDepth() { return sowingDepth; }
        public void setSowingDepth(Integer sowingDepth) { this.sowingDepth = sowingDepth; }
        public Integer getDaysToGermination() { return daysToGermination; }
        public void setDaysToGermination(Integer daysToGermination) { this.daysToGermination = daysToGermination; }
        public Integer getDaysToHarvest() { return daysToHarvest; }
        public void setDaysToHarvest(Integer daysToHarvest) { this.daysToHarvest = daysToHarvest; }
        public Boolean getCanSeedlings() { return canSeedlings; }
        public void setCanSeedlings(Boolean canSeedlings) { this.canSeedlings = canSeedlings; }
        public Boolean getCanDirectSow() { return canDirectSow; }
        public void setCanDirectSow(Boolean canDirectSow) { this.canDirectSow = canDirectSow; }
        public String getPhotoPath() { return photoPath; }
        public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    }

    @GetMapping("/crops/compatibility")
    public ResponseEntity<List<CompatibilityDTO>> getCompatibilityMatrix() {
        List<Object[]> rawData = compatibilityRepository.getRawMatrix();
        List<CompatibilityDTO> result = new ArrayList<>();
        
        for (Object[] row : rawData) {
            Integer cropId1 = row[0] != null ? ((Number) row[0]).intValue() : null;
            String crop1Name = row[1] != null ? String.valueOf(row[1]) : "";
            Integer cropId2 = row[2] != null ? ((Number) row[2]).intValue() : null;
            String crop2Name = row[3] != null ? String.valueOf(row[3]) : "";
            Integer status = row[4] != null ? ((Number) row[4]).intValue() : 1;
            
            CompatibilityDTO dto = new CompatibilityDTO();
            dto.setCrop1(crop1Name);
            dto.setCrop2(crop2Name);
            dto.setStatus(status);
            dto.setCropId1(cropId1);
            dto.setCropId2(cropId2);
            result.add(dto);
        }
        
        return ResponseEntity.ok(result);
    }
}