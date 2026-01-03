package com.example.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    // Загрузка фото растения
    @PostMapping("/upload/crop")
    public ResponseEntity<String> uploadCropImage(@RequestParam("file") MultipartFile file,
                                                  @RequestParam(value = "category", required = false) String category) {
        try {
            // Определяем папку по категории
            String folder = "crops/";
            if (category != null && !category.isEmpty()) {
                // Приводим к нижнему регистру и убираем пробелы
                String cleanCategory = category.toLowerCase().trim();
                folder += cleanCategory + "/";
            } else {
                folder += "default/";
            }
            
            return uploadFile(file, folder);
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка загрузки файла: " + e.getMessage());
        }
    }
    
    // Загрузка фото пользователя
    @PostMapping("/upload/user")
    public ResponseEntity<String> uploadUserImage(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("userId") Integer userId) {
        try {
            String folder = "users/" + userId + "/";
            return uploadFile(file, folder);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка загрузки файла: " + e.getMessage());
        }
    }
    
    // Универсальный метод загрузки
    private ResponseEntity<String> uploadFile(MultipartFile file, String subfolder) throws IOException {
        // Проверяем размер файла
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пустой");
        }
        
        // Создаем уникальное имя
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "file";
        }
        
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + fileExtension;
        
        // Полный путь с подпапкой
        String fullPath = subfolder + filename;
        Path path = Paths.get(uploadDir).resolve(fullPath);
        
        // Создаем папки если их нет
        Files.createDirectories(path.getParent());
        
        // Сохраняем файл
        Files.write(path, file.getBytes());
        
        // Возвращаем относительный URL для доступа
        String fileUrl = "/uploads/" + fullPath;
        System.out.println("File uploaded: " + path.toAbsolutePath());
        System.out.println("File URL: " + fileUrl);
        
        return ResponseEntity.ok(fileUrl);
    }
    
    // Получение файла
    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path path = Paths.get(uploadDir).resolve(filename);
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(path);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}