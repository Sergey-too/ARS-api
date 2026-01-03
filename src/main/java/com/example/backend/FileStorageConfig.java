package com.example.backend;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FileStorageConfig {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.path.crops}")
    private String cropsPath;
    
    @Value("${file.path.users}")
    private String usersPath;
    
    @PostConstruct
    public void init() {
        createDirectory(uploadDir);
        createDirectory(cropsPath);
        createDirectory(cropsPath + "/vegetables");
        createDirectory(cropsPath + "/flowers");
        createDirectory(cropsPath + "/fruits");
        createDirectory(cropsPath + "/trees");
        createDirectory(cropsPath + "/default");
        createDirectory(usersPath);
        
        // Копируй дефолтные фото если их нет
        copyDefaultImages();
    }
    
    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
            System.out.println("Создана папка: " + path);
        }
    }
    
    private void copyDefaultImages() {
        // Можно добавить копирование дефолтных изображений
        // из resources/static/images в uploads/crops/default/
    }
}