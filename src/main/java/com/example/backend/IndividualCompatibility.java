package com.example.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "individual_compatibility_crops")
public class IndividualCompatibility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "crop1_id")
    private IndividualUserCrop crop1;
    
    @ManyToOne
    @JoinColumn(name = "crop2_id")
    private IndividualUserCrop crop2;
    
    @Column(name = "compatibility")
    private Integer compatibility;
    
    @Column(name = "user_id")
    private Integer userId;
    
    public IndividualCompatibility() {}
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public IndividualUserCrop getCrop1() { return crop1; }
    public void setCrop1(IndividualUserCrop crop1) { this.crop1 = crop1; }
    
    public IndividualUserCrop getCrop2() { return crop2; }
    public void setCrop2(IndividualUserCrop crop2) { this.crop2 = crop2; }
    
    public Integer getCompatibility() { return compatibility; }
    public void setCompatibility(Integer compatibility) { this.compatibility = compatibility; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}