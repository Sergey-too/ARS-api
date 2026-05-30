package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IndividualUserCropRepository extends JpaRepository<IndividualUserCrop, Integer> {
    
    List<IndividualUserCrop> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<IndividualUserCrop> findByUserIdAndNameContainingIgnoreCase(Integer userId, String name);

    @Query(value = "SELECT * FROM individual_user_crops WHERE user_id = :userId", nativeQuery = true)
    List<IndividualUserCrop> findByUserId(@Param("userId") int userId);
}