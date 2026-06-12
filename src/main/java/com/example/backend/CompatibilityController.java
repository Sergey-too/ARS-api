package com.example.backend;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    @Autowired
    private CompatibilityService compatibilityService;
    
    @Autowired
    private CompatibilityRepository compatibilityRepository;  // ← ДОБАВИТЬ ЭТУ СТРОКУ

    @PostMapping("/update")
    public ResponseEntity<Void> updateCompatibility(@RequestBody CompatibilityDTO dto) {
        boolean isUpdated = compatibilityService.updateMatrixStatus(dto);
        
        if (isUpdated) {
            return ResponseEntity.ok().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkCompatibility(@RequestParam Integer cropId1, @RequestParam Integer cropId2) {
        Map<String, Object> response = new HashMap<>();
        Integer status = compatibilityRepository.getCompatibilityStatus(cropId1, cropId2);
        response.put("status", status != null ? status : 3);
        response.put("cropId1", cropId1);
        response.put("cropId2", cropId2);
        return ResponseEntity.ok(response);
    }
}