package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IndividualUserCropRepository extends JpaRepository<IndividualUserCrop, Integer> {
    
    List<IndividualUserCrop> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<IndividualUserCrop> findByUserIdAndNameContainingIgnoreCase(Integer userId, String name);
}