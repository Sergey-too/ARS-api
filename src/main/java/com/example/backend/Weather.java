package com.example.backend;

import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "weather_atm") 
public class Weather {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "region_id")
    private Integer regionId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "temperature_min")
    private String temperatureMin;
    
    @Column(name = "temperature_max")
    private String temperatureMax;
    
    @Column(name = "humidity_min")
    private String humidityMin;
    
    @Column(name = "humidity_max")
    private String humidityMax;
    
    @Column(name = "precipitation")
    private String precipitation;
    
    @Column(name = "wind_min")
    private String windMin;
    
    @Column(name = "wind_max")
    private String windMax;
    
    @Column(name = "gusts_of_wind")
    private String gustsOfWind;
    
    @Column(name = "pressure")
    private String pressure;

    public Weather() {}

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRegionId() { return regionId; }
    public void setRegionId(Integer regionId) { this.regionId = regionId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getTemperatureMin() { return temperatureMin; }
    public void setTemperatureMin(String temperatureMin) { this.temperatureMin = temperatureMin; }

    public String getTemperatureMax() { return temperatureMax; }
    public void setTemperatureMax(String temperatureMax) { this.temperatureMax = temperatureMax; }

    public String getHumidityMin() { return humidityMin; }
    public void setHumidityMin(String humidityMin) { this.humidityMin = humidityMin; }

    public String getHumidityMax() { return humidityMax; }
    public void setHumidityMax(String humidityMax) { this.humidityMax = humidityMax; }

    public String getPrecipitation() { return precipitation; }
    public void setPrecipitation(String precipitation) { this.precipitation = precipitation; }

    public String getWindMin() { return windMin; }
    public void setWindMin(String windMin) { this.windMin = windMin; }

    public String getWindMax() { return windMax; }
    public void setWindMax(String windMax) { this.windMax = windMax; }

    public String getGustsOfWind() { return gustsOfWind; }
    public void setGustsOfWind(String gustsOfWind) { this.gustsOfWind = gustsOfWind; }

    public String getPressure() { return pressure; }
    public void setPressure(String pressure) { this.pressure = pressure; }
}