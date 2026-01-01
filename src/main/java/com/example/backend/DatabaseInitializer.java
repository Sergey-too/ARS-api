package com.example.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Инициализация базы данных...");
        
        // Создаем таблицы если они не существуют
        createTablesIfNotExist();
        
        System.out.println("✅ База данных готова");
    }
    
    private void createTablesIfNotExist() {
        // Таблица регионов
        jdbcTemplate.execute("""
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='regions' AND xtype='U')
            CREATE TABLE regions (
                id INT IDENTITY(1,1) PRIMARY KEY,
                name VARCHAR(100) NOT NULL
            )
            """);
        
        // Таблица погоды
        jdbcTemplate.execute("""
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='weather' AND xtype='U')
            CREATE TABLE weather (
                id INT IDENTITY(1,1) PRIMARY KEY,
                region_id INT NOT NULL,
                date DATE NOT NULL,
                temperature VARCHAR(10),
                humidity VARCHAR(10),
                precipitation VARCHAR(25),
                wind VARCHAR(25),
                condition VARCHAR(50),
                FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
            )
            """);
        
        System.out.println("✅ Таблицы созданы или уже существуют");
    }
}