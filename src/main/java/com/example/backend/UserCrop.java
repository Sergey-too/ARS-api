package com.example.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "user_crops")
public class UserCrop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "crop_id", nullable = false)
    private Integer cropId;
    
    @Column(name = "area_id") 
    private Integer areaId;
    
    @ManyToOne
    @JoinColumn(name = "crop_id", insertable = false, updatable = false)
    private Crop crop;

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
    
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}