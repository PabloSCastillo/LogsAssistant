package com.dicsys.assistant;

import java.io.File;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        // 1. Detectar si el archivo .env está en la raíz actual o en la subcarpeta /assistant
        String dotenvDirectory = "./";
        if (!new File(".env").exists() && new File("assistant/.env").exists()) {
            dotenvDirectory = "./assistant";
        }

        // 2. Cargar el .env desde la ubicación encontrada
        Dotenv dotenv = Dotenv.configure()
                .directory(dotenvDirectory)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        // 3. Verificación en consola
        boolean keyCargada = System.getProperty("GEMINI_API_KEY") != null;
        System.out.println(">>> GEMINI_API_KEY cargada correctamente: " + keyCargada);

        // 4. Configuración de TimeZone y arranque
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(AssistantApplication.class, args);
    }
}