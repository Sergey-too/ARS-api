package com.example.backend;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository 
public interface IndividualCompatibilityRepository extends JpaRepository<IndividualCompatibility, Integer> {
    
    @Query(value = "SELECT * FROM individual_compatibility_crops WHERE user_id = :userId", nativeQuery = true)
    List<IndividualCompatibility> findByUserId(@Param("userId") int userId);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE individual_compatibility_crops SET compatibility = :status " +
                   "WHERE user_id = :userId AND " +
                   "((crop1_id = :crop1Id AND crop2_id = :crop2Id) " +
                   "OR (crop1_id = :crop2Id AND crop2_id = :crop1Id))", 
                   nativeQuery = true)
    int updateCompatibilityStatus(@Param("crop1Id") Integer crop1Id, 
                                  @Param("crop2Id") Integer crop2Id, 
                                  @Param("status") Integer status,
                                  @Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM IndividualCompatibility c WHERE c.crop1.id = :cropId OR c.crop2.id = :cropId")
    void deleteByCropId(@Param("cropId") Integer cropId);
}