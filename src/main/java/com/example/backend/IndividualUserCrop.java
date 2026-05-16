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

    @Column(length = 100)
    private String variety;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "min_temp") private Float minTemp;
    @Column(name = "max_temp") private Float maxTemp;
    @Column(name = "max_wind") private Float maxWind;
    @Column(name = "min_humidity") private Integer minHumidity;
    @Column(name = "max_humidity") private Integer maxHumidity;
    @Column(name = "needed_precipitation") private Float neededPrecipitation;
    @Column(name = "sowing_depth") private Integer sowingDepth;
    @Column(name = "days_to_germination") private Integer daysToGermination;
    @Column(name = "days_to_harvest") private Integer daysToHarvest;
    @Column(name = "can_seedlings") private Boolean canSeedlings;
    @Column(name = "can_direct_sow") private Boolean canDirectSow;
    @Column(name = "local_photo_path", length = 500) private String localPhotoPath;
    @Column(name = "category_id") private Integer categoryId;

    @Column(name = "watering_interval") private Integer wateringInterval = 3;
    @Column(name = "fertilizing_interval") private Integer fertilizingInterval = 14;
    @Column(name = "soil_care_interval") private Integer soilCareInterval = 7;
    @Column(name = "protection_interval") private Integer protectionInterval;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Float getMinTemp() { return minTemp; }
    public void setMinTemp(Float minTemp) { this.minTemp = minTemp; }
    public Float getMaxTemp() { return maxTemp; }
    public void setMaxTemp(Float maxTemp) { this.maxTemp = maxTemp; }
    public Float getMaxWind() { return maxWind; }
    public void setMaxWind(Float maxWind) { this.maxWind = maxWind; }
    public Integer getMinHumidity() { return minHumidity; }
    public void setMinHumidity(Integer minHumidity) { this.minHumidity = minHumidity; }
    public Integer getMaxHumidity() { return maxHumidity; }
    public void setMaxHumidity(Integer maxHumidity) { this.maxHumidity = maxHumidity; }
    public Float getNeededPrecipitation() { return neededPrecipitation; }
    public void setNeededPrecipitation(Float neededPrecipitation) { this.neededPrecipitation = neededPrecipitation; }
    public Integer getSowingDepth() { return sowingDepth; }
    public void setSowingDepth(Integer sowingDepth) { this.sowingDepth = sowingDepth; }
    public Integer getDaysToGermination() { return daysToGermination; }
    public void setDaysToGermination(Integer daysToGermination) { this.daysToGermination = daysToGermination; }
    public Integer getDaysToHarvest() { return daysToHarvest; }
    public void setDaysToHarvest(Integer daysToHarvest) { this.daysToHarvest = daysToHarvest; }
    public Boolean getCanSeedlings() { return canSeedlings; }
    public void setCanSeedlings(Boolean canSeedlings) { this.canSeedlings = canSeedlings; }
    public Boolean getCanDirectSow() { return canDirectSow; }
    public void setCanDirectSow(Boolean canDirectSow) { this.canDirectSow = canDirectSow; }
    public String getLocalPhotoPath() { return localPhotoPath; }
    public void setLocalPhotoPath(String localPhotoPath) { this.localPhotoPath = localPhotoPath; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getWateringInterval() { return wateringInterval; }
    public void setWateringInterval(Integer wateringInterval) { this.wateringInterval = wateringInterval; }
    public Integer getFertilizingInterval() { return fertilizingInterval; }
    public void setFertilizingInterval(Integer fertilizingInterval) { this.fertilizingInterval = fertilizingInterval; }
    public Integer getSoilCareInterval() { return soilCareInterval; }
    public void setSoilCareInterval(Integer soilCareInterval) { this.soilCareInterval = soilCareInterval; }
    public Integer getProtectionInterval() { return protectionInterval; }
    public void setProtectionInterval(Integer protectionInterval) { this.protectionInterval = protectionInterval; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}