package com.example.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_crops")
public class UserCrop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    // Может быть null, если это IndividualUserCrop
    @Column(name = "crop_id") 
    private Integer cropId;

    // Ссылка на личное растение пользователя
    @Column(name = "individual_crop_id")
    private Integer individualCropId;
    
    @Column(name = "area_id") 
    private Integer areaId;

    @Column(name = "planted_at")
    private LocalDateTime plantedAt;

    @Column(name = "status")
    private String status = "planted"; // 'planted', 'harvested', 'list'

    @ManyToOne
    @JoinColumn(name = "crop_id", insertable = false, updatable = false)
    private Crop crop;

    @ManyToOne
    @JoinColumn(name = "individual_crop_id", insertable = false, updatable = false)
    private IndividualUserCrop individualCrop;

    @ManyToOne
    @JoinColumn(name = "area_id", insertable = false, updatable = false)
    private Area area;
    
    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getCropId() { return cropId; }
    public void setCropId(Integer cropId) { this.cropId = cropId; }
    public Integer getIndividualCropId() { return individualCropId; }
    public void setIndividualCropId(Integer individualCropId) { this.individualCropId = individualCropId; }
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    public LocalDateTime getPlantedAt() { return plantedAt; }
    public void setPlantedAt(LocalDateTime plantedAt) { this.plantedAt = plantedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public IndividualUserCrop getIndividualCrop() { return individualCrop; }
    public void setIndividualCrop(IndividualUserCrop individualCrop) { this.individualCrop = individualCrop; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}