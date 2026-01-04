package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
// public interface UserCropRepository extends JpaRepository<UserCrop, Integer> {
    
//     // Текущий метод не загружает растение
//     List<UserCrop> findByUserId(Integer userId);
    
//     // Создай новый метод с загрузкой растения
//     @Query("SELECT uc FROM UserCrop uc LEFT JOIN FETCH uc.crop LEFT JOIN FETCH uc.crop.category WHERE uc.userId = :userId")
//     List<UserCrop> findByUserIdWithCropDetails(@Param("userId") Integer userId);
    
//     void deleteByUserIdAndCropId(Integer userId, Integer cropId);
// }   
public interface UserCropRepository extends JpaRepository<UserCrop, Integer> {
    
    // Метод должен ЗАГРУЖАТЬ связанные данные
    @Query("SELECT DISTINCT uc FROM UserCrop uc " +
           "LEFT JOIN FETCH uc.crop c " +
           "LEFT JOIN FETCH c.category " +
           "WHERE uc.userId = :userId")
    List<UserCrop> findByUserIdWithCropDetails(@Param("userId") Integer userId);
    
    List<UserCrop> findByUserId(Integer userId);
    
    void deleteByUserIdAndCropId(Integer userId, Integer cropId);
}