package com.example.backend;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import org.springframework.security.crypto.bcrypt.BCrypt;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "login", nullable = false, length = 50)
    private String login;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false; 

    @Column(name = "in_ban", nullable = false)
    private boolean inBan = false;  

    public User() {
        this.registrationDate = LocalDateTime.now();
        this.isAdmin = false;
        this.inBan = false;
    }
    
    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { 
        this.registrationDate = registrationDate; 
    }

    public void setPassword(String password) {
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isInBan() { return inBan; }
    public void setInBan(boolean ban) { inBan = ban; }
}