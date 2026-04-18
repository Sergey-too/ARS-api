package com.example.backend;

public class CompatibilityDTO {
    private String crop1;
    private String crop2;
    private Integer status;

    public CompatibilityDTO(String crop1, String crop2, Integer status) {
        this.crop1 = crop1;
        this.crop2 = crop2;
        this.status = status;
    }

    public String getCrop1() { return crop1; }
    public String getCrop2() { return crop2; }
    public Integer getStatus() { return status; }
}