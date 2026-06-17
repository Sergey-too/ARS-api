package com.example.backend;

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
@RequestMapping("/api/gardens")
@CrossOrigin(origins = "*")
public class GardenController {
    
    @Autowired private GardenRepository gardenRepository;
    @Autowired private GardenAreaRepository gardenAreaRepository;
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Garden>> getUserGardens(@PathVariable Integer userId) {
        List<Garden> gardens = gardenRepository.findByUserId(userId);
        for (Garden garden : gardens) {
            List<Area> areas = gardenAreaRepository.findAreasByGardenId(garden.getId());
            garden.setAreas(areas);
        }
        return ResponseEntity.ok(gardens);
    }
    
    @PostMapping
    public ResponseEntity<Garden> createGarden(@RequestBody Garden garden) {
        return ResponseEntity.ok(gardenRepository.save(garden));
    }

    @GetMapping("/{gardenId}/areas")
    public ResponseEntity<List<Area>> getGardenAreas(@PathVariable Integer gardenId) {
        List<Area> areas = gardenRepository.findAreasByGardenId(gardenId);
        return ResponseEntity.ok(areas);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Garden> updateGarden(@PathVariable Integer id, @RequestBody Garden garden) {
        garden.setId(id);
        return ResponseEntity.ok(gardenRepository.save(garden));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGarden(@PathVariable Integer id) {
        gardenAreaRepository.deleteByGardenId(id);
        gardenRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{gardenId}/areas")
    public ResponseEntity<?> addAreaToGarden(@PathVariable Integer gardenId, @RequestBody Map<String, Integer> request) {
        Integer areaId = request.get("areaId");
        GardenArea ga = new GardenArea();
        ga.setGardenId(gardenId);
        ga.setAreaId(areaId);
        gardenAreaRepository.save(ga);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{gardenId}/areas/{areaId}")
    public ResponseEntity<?> removeAreaFromGarden(@PathVariable Integer gardenId, @PathVariable Integer areaId) {
        gardenAreaRepository.deleteById(new GardenAreaId(gardenId, areaId));
        return ResponseEntity.ok().build();
    }
}