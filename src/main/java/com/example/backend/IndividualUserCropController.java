package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-crops")
public class IndividualUserCropController {

    @Autowired
    private IndividualUserCropRepository repository;

    // 1. Получить все растения конкретного юзера
    @GetMapping("/user/{userId}")
    public List<IndividualUserCrop> getCropsByUserId(@PathVariable Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 2. Получить конкретное растение по ID
    @GetMapping("/{id}")
    public ResponseEntity<IndividualUserCrop> getCropById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Создать новую запись
    @PostMapping
    public ResponseEntity<IndividualUserCrop> createCrop(@RequestBody IndividualUserCrop crop) {
        try {
            IndividualUserCrop savedCrop = repository.save(crop);
            return new ResponseEntity<>(savedCrop, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 4. Обновить существующую запись
    @PutMapping("/{id}")
    public ResponseEntity<IndividualUserCrop> updateCrop(@PathVariable Integer id, @RequestBody IndividualUserCrop details) {
        return repository.findById(id).map(crop -> {
            crop.setName(details.getName());
            crop.setDescription(details.getDescription());
            crop.setMinTemp(details.getMinTemp());
            crop.setMaxTemp(details.getMaxTemp());
            crop.setMaxWind(details.getMaxWind());
            crop.setMinHumidity(details.getMinHumidity());
            crop.setMaxHumidity(details.getMaxHumidity());
            crop.setNeededPrecipitation(details.getNeededPrecipitation());
            crop.setSowingDepth(details.getSowingDepth());
            crop.setDaysToGermination(details.getDaysToGermination());
            crop.setDaysToHarvest(details.getDaysToHarvest());
            crop.setCanSeedlings(details.isCanSeedlings());
            crop.setCanDirectSow(details.isCanDirectSow());
            crop.setLocalPhotoPath(details.getLocalPhotoPath());
            crop.setCategoryId(details.getCategoryId());
            return ResponseEntity.ok(repository.save(crop));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Удалить растение
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCrop(@PathVariable Integer id) {
        try {
            repository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}