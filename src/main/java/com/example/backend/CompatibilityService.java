package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompatibilityService {

    @Autowired
    private CompatibilityRepository compatibilityRepository;
    
    @Autowired
    private CropRepository cropRepository;

    @Transactional
    public boolean updateMatrixStatus(CompatibilityDTO dto) {
        int updatedRows = compatibilityRepository.updateCompatibilityStatus(
                dto.getCrop1(), 
                dto.getCrop2(), 
                dto.getStatus()
        );
        
        if (updatedRows == 0) {
            Crop crop1 = cropRepository.findByName(dto.getCrop1()).orElse(null);
            Crop crop2 = cropRepository.findByName(dto.getCrop2()).orElse(null);
            
            if (crop1 != null && crop2 != null) {
                Compatibility compatibility = new Compatibility();
                compatibility.setCrop1(crop1);
                compatibility.setCrop2(crop2);
                compatibility.setCompatibility(dto.getStatus());
                compatibilityRepository.save(compatibility);
                return true;
            }
            return false;
        }
        
        return true;
    }
}