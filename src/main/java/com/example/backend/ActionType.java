package com.example.backend;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "action_types")
public class ActionType implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    public ActionType() {}
    
    public ActionType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}