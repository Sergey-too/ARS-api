package com.example.backend;

public class IndividualCompatibilityDTO {
    private Integer crop1Id;
    private Integer crop2Id;
    private Integer status;
    private Integer userId;
    private String crop1Name;
    private String crop2Name;

    public IndividualCompatibilityDTO() {}

    public IndividualCompatibilityDTO(Integer crop1Id, Integer crop2Id, Integer status, Integer userId) {
        this.crop1Id = crop1Id;
        this.crop2Id = crop2Id;
        this.status = status;
        this.userId = userId;
    }

    public Integer getCrop1Id() { return crop1Id; }
    public void setCrop1Id(Integer crop1Id) { this.crop1Id = crop1Id; }

    public Integer getCrop2Id() { return crop2Id; }
    public void setCrop2Id(Integer crop2Id) { this.crop2Id = crop2Id; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getCrop1Name() { return crop1Name; }
    public void setCrop1Name(String crop1Name) { this.crop1Name = crop1Name; }

    public String getCrop2Name() { return crop2Name; }
    public void setCrop2Name(String crop2Name) { this.crop2Name = crop2Name; }
}