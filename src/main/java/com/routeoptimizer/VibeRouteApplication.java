package com.routeoptimizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VibeRouteApplication {

  public static void main(String[] args) {
    // Carga de variables de entorno desde .env (Soporte para raíz y carpeta backend)
    io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load();
    dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

    // Intento de carga desde carpeta superior si no se encontró en la actual
    if (System.getProperty("GOOGLE_MAPS_KEY") == null) {
      io.github.cdimascio.dotenv.Dotenv rootDotenv = io.github.cdimascio.dotenv.Dotenv.configure()
          .directory("..")
          .ignoreIfMalformed()
          .ignoreIfMissing()
          .load();
      rootDotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }

    SpringApplication.run(VibeRouteApplication.class, args);
  }

}
