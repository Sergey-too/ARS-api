    package com.example.backend;

    import com.fasterxml.jackson.annotation.JsonBackReference;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.Table;

    @Entity
    @Table(name = "user_crops")
    public class UserCrop {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;
        
        @Column(name = "user_id", nullable = false)
        private Integer userId;
        
        @Column(name = "crop_id", nullable = false)
        private Integer cropId;
        
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "crop_id", insertable = false, updatable = false)
        @JsonBackReference 
        private Crop crop;
        
        // Геттеры и сеттеры
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        
        public Integer getCropId() { return cropId; }
        public void setCropId(Integer cropId) { this.cropId = cropId; }
        
        public Crop getCrop() { return crop; }
        public void setCrop(Crop crop) { this.crop = crop; }
    }