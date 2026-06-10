package com.example.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository 
public interface CompatibilityRepository extends JpaRepository<Compatibility, Integer> {  // ← исправлено!

    @Query(value = "EXEC GetCropsCompatibilityMatrix", nativeQuery = true)
    List<Object[]> getRawMatrix();

    @Modifying
    @Transactional
    @Query(value = "UPDATE compatibility_crops SET compatibility = :status " +
                   "WHERE (id_crop1 = (SELECT id FROM crops WHERE name = :crop1) AND id_crop2 = (SELECT id FROM crops WHERE name = :crop2)) " +
                   "OR (id_crop1 = (SELECT id FROM crops WHERE name = :crop2) AND id_crop2 = (SELECT id FROM crops WHERE name = :crop1))", 
                   nativeQuery = true)
    int updateCompatibilityStatus(@Param("crop1") String crop1, 
                                  @Param("crop2") String crop2, 
                                  @Param("status") Integer status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Compatibility c WHERE c.crop1.id = :cropId OR c.crop2.id = :cropId")
    void deleteByCropId(@Param("cropId") Integer cropId);
}