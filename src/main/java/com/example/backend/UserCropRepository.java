package com.example.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserCropRepository extends JpaRepository<UserCrop, Integer> {

    List<UserCrop> findByUserId(Integer userId);

    List<UserCrop> findByCropId(Integer cropId);

    @Query("SELECT uc FROM UserCrop uc " +
           "LEFT JOIN FETCH uc.crop " + 
           "LEFT JOIN FETCH uc.individualCrop " +
           "WHERE uc.userId = :userId")
    List<UserCrop> findByUserIdWithDetails(@Param("userId") Integer userId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(Integer userId);
}