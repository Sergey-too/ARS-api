package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying; 
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserCropRepository extends JpaRepository<UserCrop, Integer> {
    
    @Query("SELECT DISTINCT uc FROM UserCrop uc " +
           "LEFT JOIN FETCH uc.crop c " +
           "LEFT JOIN FETCH c.category " +
           "WHERE uc.userId = :userId")
    List<UserCrop> findByUserIdWithCropDetails(@Param("userId") Integer userId);
    
    @Query("SELECT uc FROM UserCrop uc WHERE uc.userId = :userId")
    List<UserCrop> findByUserId(@Param("userId") Integer userId);

    void deleteByUserIdAndCropId(Integer userId, Integer cropId);

    @Query("SELECT uc FROM UserCrop uc WHERE uc.cropId = :cropId")
    List<UserCrop> findByCropId(@Param("cropId") Integer cropId);

    @Query("SELECT COUNT(uc) > 0 FROM UserCrop uc WHERE uc.userId = :userId AND uc.cropId = :cropId")
    boolean existsByUserIdAndCropId(@Param("userId") Integer userId, @Param("cropId") Integer cropId);

    @Transactional
    @Modifying
    void deleteByUserId(Integer userId);
}