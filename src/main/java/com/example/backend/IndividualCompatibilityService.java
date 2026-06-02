package com.example.backend;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndividualCompatibilityService {

    @Autowired
    private IndividualCompatibilityRepository individualCompatibilityRepository;
    
    @Autowired
    private IndividualUserCropRepository individualUserCropRepository;

    @Transactional
    public void update(IndividualCompatibilityDTO dto) {
        int updatedRows = individualCompatibilityRepository.updateCompatibilityStatus(
            dto.getCrop1Id(), 
            dto.getCrop2Id(), 
            dto.getStatus(),
            dto.getUserId()
        );
        
        // Если ничего не обновилось - значит записи нет, нужно создать
        if (updatedRows == 0) {
            // Проверяем, существует ли растение
            IndividualUserCrop crop1 = individualUserCropRepository.findById(dto.getCrop1Id()).orElse(null);
            IndividualUserCrop crop2 = individualUserCropRepository.findById(dto.getCrop2Id()).orElse(null);
            
            if (crop1 != null && crop2 != null) {
                IndividualCompatibility compatibility = new IndividualCompatibility();
                compatibility.setCrop1(crop1);
                compatibility.setCrop2(crop2);
                compatibility.setCompatibility(dto.getStatus());
                compatibility.setUserId(dto.getUserId());
                
                individualCompatibilityRepository.save(compatibility);
            }
        }
    }
    
    public List<IndividualCompatibilityDTO> getMatrixByUser(int userId) {
        List<IndividualCompatibility> compatibilities = individualCompatibilityRepository.findByUserId(userId);
        List<IndividualUserCrop> userCrops = individualUserCropRepository.findByUserId(userId);
        
        List<IndividualCompatibilityDTO> result = new ArrayList<>();
        
        for (int i = 0; i < userCrops.size(); i++) {
            for (int j = 0; j < userCrops.size(); j++) {
                IndividualUserCrop crop1 = userCrops.get(i);
                IndividualUserCrop crop2 = userCrops.get(j);
                
                int compatibility = 1; 

                for (IndividualCompatibility ic : compatibilities) {
                    if ((ic.getCrop1().getId().equals(crop1.getId()) && ic.getCrop2().getId().equals(crop2.getId())) ||
                        (ic.getCrop1().getId().equals(crop2.getId()) && ic.getCrop2().getId().equals(crop1.getId()))) {
                        compatibility = ic.getCompatibility() != null ? ic.getCompatibility() : 1;
                        break;
                    }
                }
                
                IndividualCompatibilityDTO dto = new IndividualCompatibilityDTO();
                dto.setCrop1Id(crop1.getId());
                dto.setCrop2Id(crop2.getId());
                dto.setCrop1Name(crop1.getName());
                dto.setCrop2Name(crop2.getName());
                dto.setStatus(compatibility);
                dto.setUserId(userId);
                
                result.add(dto);
            }
        }
        
        return result;
    }
}