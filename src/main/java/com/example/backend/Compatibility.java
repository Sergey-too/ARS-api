package com.example.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "compatibility_crops")
public class Compatibility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "id_crop1")
    private Crop crop1;
    
    @ManyToOne
    @JoinColumn(name = "id_crop2")
    private Crop crop2;
    
    @Column(name = "compatibility")
    private Integer compatibility;
        
    public Compatibility() {}
    
    public Compatibility(Crop crop1, Crop crop2, Integer compatibility) {
        this.crop1 = crop1;
        this.crop2 = crop2;
        this.compatibility = compatibility;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Crop getCrop1() { return crop1; }
    public void setCrop1(Crop crop1) { this.crop1 = crop1; }
    
    public Crop getCrop2() { return crop2; }
    public void setCrop2(Crop crop2) { this.crop2 = crop2; }
    
    public Integer getCompatibility() { return compatibility; }
    public void setCompatibility(Integer compatibility) { this.compatibility = compatibility; }
}