package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaRepository areaRepository;

    @GetMapping("/user/{userId}")
    public List<Area> getUserAreas(@PathVariable Integer userId) {
        return areaRepository.findByUserId(userId);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateArea(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        return areaRepository.findById(id).map(area -> {
            area.setName((String) request.get("name"));
            area.setRegionId(((Number) request.get("regionId")).intValue());
            areaRepository.save(area);
            response.put("success", true);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }   

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteArea(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            areaRepository.deleteById(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Участок связан с растениями");
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addArea(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Area area = new Area();
            area.setName((String) request.get("name"));
            area.setRegionId(((Number) request.get("regionId")).intValue());
            area.setUserId(((Number) request.get("userId")).intValue());

            areaRepository.save(area);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}   

