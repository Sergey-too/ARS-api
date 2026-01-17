package com.example.backend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "crops")
public class Crop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore // Полностью игнорируем при сериализации
    private Category category;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "min_temp")
    private Float minTemp;
    
    @Column(name = "max_temp")
    private Float maxTemp;
    
    @Column(name = "max_wind")
    private Float maxWind;
    
    @Column(name = "min_humidity")
    private Integer minHumidity;
    
    @Column(name = "max_humidity")
    private Integer maxHumidity;
    
    @Column(name = "needed_precipitation")
    private Float neededPrecipitation;
    
    @Column(name = "sowing_depth")
    private Integer sowingDepth;
    
    @Column(name = "days_to_germination")
    private Integer daysToGermination;
    
    @Column(name = "days_to_harvest")
    private Integer daysToHarvest;
    
    @Column(name = "can_seedlings")
    private Boolean canSeedlings;
    
    @Column(name = "can_direct_sow")
    private Boolean canDirectSow;
    
    @Column(name = "photo_path", length = 255)
    private String photoPath;
    
    // Геттер для названия категории (для Android)
    @JsonProperty("category") // Это поле будет в JSON как "category"
    @Transient // Не сохраняется в БД
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
    
    // Сеттер для названия категории (для приема данных от Android)
    @Transient
    public void setCategory(String categoryName) {
        // Этот сеттер нужен только для приема данных
        // Фактическая связь устанавливается в контроллере
    }
    
    // Стандартные геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // Только для внутреннего использования в Spring
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
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
    public void setNeededPrecipitation(Float neededPrecipitation) { 
        this.neededPrecipitation = neededPrecipitation; 
    }
    
    public Integer getSowingDepth() { return sowingDepth; }
    public void setSowingDepth(Integer sowingDepth) { this.sowingDepth = sowingDepth; }
    
    public Integer getDaysToGermination() { return daysToGermination; }
    public void setDaysToGermination(Integer daysToGermination) { 
        this.daysToGermination = daysToGermination; 
    }
    
    public Integer getDaysToHarvest() { return daysToHarvest; }
    public void setDaysToHarvest(Integer daysToHarvest) { this.daysToHarvest = daysToHarvest; }
    
    public Boolean getCanSeedlings() { return canSeedlings; }
    public void setCanSeedlings(Boolean canSeedlings) { this.canSeedlings = canSeedlings; }
    
    public Boolean getCanDirectSow() { return canDirectSow; }
    public void setCanDirectSow(Boolean canDirectSow) { this.canDirectSow = canDirectSow; }
    
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}