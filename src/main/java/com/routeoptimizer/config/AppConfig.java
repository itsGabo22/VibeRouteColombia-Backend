package com.routeoptimizer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración global del sistema.
 * Define beans necesarios para la integración con servicios externos (IA,
 * Mapas)
 * y para el manejo eficiente de datos.
 */
@Configuration
public class AppConfig {

  /**
   * Cliente HTTP para consumir APIs externas (Google Maps, Modelos de IA).
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Motor de mapeo para transformar Entidades a DTOs y viceversa de forma
   * automática.
   */
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration()
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
    return modelMapper;
  }

  /**
   * Personalización del ObjectMapper de Jackson para manejo correcto de fechas
   * Java 8+.
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
