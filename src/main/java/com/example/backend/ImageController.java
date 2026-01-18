package com.example.backend;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class ImageController {
    
    @GetMapping("/img/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            System.out.println("=== GET IMAGE: " + filename + " ===");
            
            // 1. Проверяем разные варианты путей
            String[] possiblePaths = {
                "uploads/" + filename,           // uploads/crops/woods/pear.jpg
                "uploads/crops/" + filename,     // uploads/crops/crops/woods/pear.jpg (если filename начинается с crops/)
                filename                          // crops/woods/pear.jpg напрямую
            };
            
            File file = null;
            
            for (String path : possiblePaths) {
                file = new File(path);
                System.out.println("Checking: " + file.getAbsolutePath() + 
                                 " - exists: " + file.exists());
                
                if (file.exists() && file.isFile()) {
                    System.out.println("✓ File found at: " + file.getAbsolutePath());
                    break;
                }
            }
            
            if (file == null || !file.exists()) {
                System.out.println("✗ File NOT found in any location");
                return ResponseEntity.notFound().build();
            }
            
            // 2. Создаем Resource из файла
            Path filePath = file.toPath();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                // 3. Определяем Content-Type
                String contentType = "image/jpeg"; // по умолчанию
                
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (fileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (fileName.endsWith(".webp")) {
                    contentType = "image/webp";
                }
                
                System.out.println("✓ Serving file: " + file.getAbsolutePath() + 
                                 " (" + file.length() + " bytes, " + contentType + ")");
                
                // 4. Возвращаем файл
                return ResponseEntity.ok()
                        .contentType(MediaType.valueOf(contentType))
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=86400") // Кеширование на 24 часа
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "inline; filename=\"" + file.getName() + "\"")
                        .body(resource);
            } else {
                System.out.println("✗ Resource not readable");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Дополнительный метод для тестирования
    @GetMapping("/img/test/{filename}")
    public ResponseEntity<String> testImage(@PathVariable String filename) {
        StringBuilder result = new StringBuilder();
        result.append("Testing file: ").append(filename).append("\n\n");
        
        String[] paths = {
            "uploads/" + filename,
            "uploads/crops/" + filename,
            filename
        };
        
        for (String path : paths) {
            File file = new File(path);
            result.append("Path: ").append(file.getAbsolutePath())
                  .append(" - exists: ").append(file.exists())
                  .append(" - isFile: ").append(file.isFile())
                  .append(" - size: ").append(file.exists() ? file.length() + " bytes" : "N/A")
                  .append("\n");
        }
        
        return ResponseEntity.ok(result.toString());
    }
}