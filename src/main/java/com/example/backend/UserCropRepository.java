package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCropRepository extends JpaRepository<UserCrop, Integer> {
    
    List<UserCrop> findByUserId(Integer userId);
    
    void deleteByUserIdAndCropId(Integer userId, Integer cropId);
}