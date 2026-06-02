package com.example.backend;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_crops")
public class UserCrop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "crop_id") 
    private Integer cropId;

    @Column(name = "individual_crop_id")
    private Integer individualCropId;
    
    @Column(name = "area_id") 
    private Integer areaId;
    
    @Column(name = "garden_id") 
    private Integer gardenId;

    @Column(name = "planted_at")
    private LocalDate plantedAt;  
    
    @Column(name = "harvested_at")
    private LocalDate harvestedAt;  

    @ManyToOne
    @JoinColumn(name = "crop_id", insertable = false, updatable = false)
    private Crop crop;

    @ManyToOne
    @JoinColumn(name = "individual_crop_id", insertable = false, updatable = false)
    private IndividualUserCrop individualCrop;

    @ManyToOne
    @JoinColumn(name = "area_id", insertable = false, updatable = false)
    private Area area;
    
    @ManyToOne
    @JoinColumn(name = "garden_id", insertable = false, updatable = false)
    private Garden garden;

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
    
    public Integer getGardenId() { return gardenId; }
    public void setGardenId(Integer gardenId) { this.gardenId = gardenId; }
    
    public LocalDate getPlantedAt() { return plantedAt; }
    public void setPlantedAt(LocalDate plantedAt) { this.plantedAt = plantedAt; }
    
    public LocalDate getHarvestedAt() { return harvestedAt; }
    public void setHarvestedAt(LocalDate harvestedAt) { this.harvestedAt = harvestedAt; }
    
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    
    public IndividualUserCrop getIndividualCrop() { return individualCrop; }
    public void setIndividualCrop(IndividualUserCrop individualCrop) { this.individualCrop = individualCrop; }
    
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    
    public Garden getGarden() { return garden; }
    public void setGarden(Garden garden) { this.garden = garden; }
}