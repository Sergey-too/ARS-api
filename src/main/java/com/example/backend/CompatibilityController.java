package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    @Autowired
    private CompatibilityService compatibilityService;

    @PostMapping("/update")
    public ResponseEntity<Void> updateCompatibility(@RequestBody CompatibilityDTO dto) {
        boolean isUpdated = compatibilityService.updateMatrixStatus(dto);
        
        if (isUpdated) {
            return ResponseEntity.ok().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }
}