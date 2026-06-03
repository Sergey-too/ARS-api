package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface IndividualUserCropRepository extends JpaRepository<IndividualUserCrop, Integer> {
    
    List<IndividualUserCrop> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<IndividualUserCrop> findByUserIdAndNameContainingIgnoreCase(Integer userId, String name);

    @Query(value = "SELECT * FROM individual_user_crops WHERE user_id = :userId", nativeQuery = true)
    List<IndividualUserCrop> findByUserId(@Param("userId") int userId);

    @Modifying
    @Transactional
    @Query("UPDATE IndividualUserCrop i SET i.userCategoryId = NULL WHERE i.userCategoryId = :categoryId")
    void setUserCategoryIdToNull(@Param("categoryId") Integer categoryId);

    Optional<IndividualUserCrop> findByUserIdAndName(Integer userId, String name);
    
    @Query("SELECT i FROM IndividualUserCrop i WHERE i.userId = :userId")
    List<IndividualUserCrop> findAllByUserId(@Param("userId") Integer userId);
}