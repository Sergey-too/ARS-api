package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompatibilityService {

    @Autowired
    private CompatibilityRepository compatibilityRepository;

    public boolean updateMatrixStatus(CompatibilityDTO dto) {
        int updatedRows = compatibilityRepository.updateCompatibilityStatus(
                dto.getCrop1(), 
                dto.getCrop2(), 
                dto.getStatus()
        );  
        return updatedRows > 0;
    }
}