package com.example.backend;

public class WeatherComparisonDTO {
    private String monthName;
    private Double avgFactTemp;
    private Double normalTemp;
    private Double avgFactHumidity;
    private Double normalHumidity;

    public WeatherComparisonDTO() {
    }

    public WeatherComparisonDTO(String monthName, Double avgFactTemp, Double normalTemp, Double avgFactHumidity, Double normalHumidity) {
        this.monthName = monthName;
        this.avgFactTemp = avgFactTemp;
        this.normalTemp = normalTemp;
        this.avgFactHumidity = avgFactHumidity;
        this.normalHumidity = normalHumidity;
    }

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    public Double getAvgFactTemp() {
        return avgFactTemp;
    }

    public void setAvgFactTemp(Double avgFactTemp) {
        this.avgFactTemp = avgFactTemp;
    }

    public Double getNormalTemp() {
        return normalTemp;
    }

    public void setNormalTemp(Double normalTemp) {
        this.normalTemp = normalTemp;
    }

    public Double getAvgFactHumidity() {
        return avgFactHumidity;
    }

    public void setAvgFactHumidity(Double avgFactHumidity) {
        this.avgFactHumidity = avgFactHumidity;
    }

    public Double getNormalHumidity() {
        return normalHumidity;
    }

    public void setNormalHumidity(Double normalHumidity) {
        this.normalHumidity = normalHumidity;
    }

    
}