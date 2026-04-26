package com.example.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "individual_user_crops")
public class IndividualUserCrop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "min_temp") private Double minTemp;
    @Column(name = "max_temp") private Double maxTemp;
    @Column(name = "max_wind") private Double maxWind;
    @Column(name = "min_humidity") private Double minHumidity;
    @Column(name = "max_humidity") private Double maxHumidity;
    @Column(name = "needed_precipitation") private Double neededPrecipitation;
    @Column(name = "sowing_depth") private Double sowingDepth;
    
    @Column(name = "days_to_germination") private Integer daysToGermination;
    @Column(name = "days_to_harvest") private Integer daysToHarvest;
    
    @Column(name = "can_seedlings") private boolean canSeedlings;
    @Column(name = "can_direct_sow") private boolean canDirectSow;
    
    @Column(name = "local_photo_path", length = 500) 
    private String localPhotoPath;

    @Column(name = "category_id") 
    private Integer categoryId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public IndividualUserCrop() {}

    // --- Геттеры и Сеттеры ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getMinTemp() { return minTemp; }
    public void setMinTemp(Double minTemp) { this.minTemp = minTemp; }

    public Double getMaxTemp() { return maxTemp; }
    public void setMaxTemp(Double maxTemp) { this.maxTemp = maxTemp; }

    public Double getMaxWind() { return maxWind; }
    public void setMaxWind(Double maxWind) { this.maxWind = maxWind; }

    public Double getMinHumidity() { return minHumidity; }
    public void setMinHumidity(Double minHumidity) { this.minHumidity = minHumidity; }

    public Double getMaxHumidity() { return maxHumidity; }
    public void setMaxHumidity(Double maxHumidity) { this.maxHumidity = maxHumidity; }

    public Double getNeededPrecipitation() { return neededPrecipitation; }
    public void setNeededPrecipitation(Double neededPrecipitation) { this.neededPrecipitation = neededPrecipitation; }

    public Double getSowingDepth() { return sowingDepth; }
    public void setSowingDepth(Double sowingDepth) { this.sowingDepth = sowingDepth; }

    public Integer getDaysToGermination() { return daysToGermination; }
    public void setDaysToGermination(Integer daysToGermination) { this.daysToGermination = daysToGermination; }

    public Integer getDaysToHarvest() { return daysToHarvest; }
    public void setDaysToHarvest(Integer daysToHarvest) { this.daysToHarvest = daysToHarvest; }

    public boolean isCanSeedlings() { return canSeedlings; }
    public void setCanSeedlings(boolean canSeedlings) { this.canSeedlings = canSeedlings; }

    public boolean isCanDirectSow() { return canDirectSow; }
    public void setCanDirectSow(boolean canDirectSow) { this.canDirectSow = canDirectSow; }

    public String getLocalPhotoPath() { return localPhotoPath; }
    public void setLocalPhotoPath(String localPhotoPath) { this.localPhotoPath = localPhotoPath; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}