package com.example.backend;

public class CompatibilityDTO {
    private String crop1;
    private String crop2;
    private Integer status;
    private Integer cropId1;
    private Integer cropId2;

    public CompatibilityDTO() {}

    public CompatibilityDTO(String crop1, String crop2, Integer status, Integer cropId1, Integer cropId2) {
        this.crop1 = crop1;
        this.crop2 = crop2;
        this.status = status;
        this.cropId1 = cropId1;
        this.cropId2 = cropId2;
    }

    // Геттеры и сеттеры
    public String getCrop1() { return crop1; }
    public void setCrop1(String crop1) { this.crop1 = crop1; }
    
    public String getCrop2() { return crop2; }
    public void setCrop2(String crop2) { this.crop2 = crop2; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public Integer getCropId1() { return cropId1; }
    public void setCropId1(Integer cropId1) { this.cropId1 = cropId1; }
    
    public Integer getCropId2() { return cropId2; }
    public void setCropId2(Integer cropId2) { this.cropId2 = cropId2; }
}