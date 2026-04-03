package com.routeoptimizer.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para la API de Google Gemini.
 * Responsabilidad única: enviar un prompt de texto y recibir la respuesta
 * generada.
 * Implementa fallback graceful cuando la API key no está configurada o hay
 * errores.
 */
@Component
public class GeminiClient {

  private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
  private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${gemini.api.key:}")
  private String apiKey;

  @Value("${gemini.model:gemini-2.0-flash}")
  private String model;

  public GeminiClient() {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Envía un prompt a Gemini y retorna el texto generado.
   * Si la API key no está configurada o hay un error, retorna el fallback.
   *
   * @param prompt   El texto del prompt para Gemini
   * @param fallback Texto a retornar si la API falla
   * @return Respuesta generada por Gemini o el fallback
   */
  @SuppressWarnings("null")
  public String generateText(String prompt, String fallback) {
    if (apiKey == null || apiKey.isBlank()) {
      log.warn("Gemini API Key no configurada. Usando respuesta fallback.");
      return fallback;
    }

    try {
      String url = String.format(GEMINI_URL, model, apiKey);

      // Construir el body según la especificación de Gemini API
      String requestBody = objectMapper.writeValueAsString(
          new java.util.HashMap<String, Object>() {
            {
              put("contents", new Object[] {
                  new java.util.HashMap<String, Object>() {
                    {
                      put("parts", new Object[] {
                          new java.util.HashMap<String, String>() {
                            {
                              put("text", prompt);
                            }
                          }
                      });
                    }
                  }
              });
            }
          });

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

      String response = restTemplate.postForObject(url, entity, String.class);
      if (response == null) {
        log.warn("Gemini retornó respuesta nula. Usando fallback.");
        return fallback;
      }
      JsonNode root = objectMapper.readTree(response);

      // Extraer el texto de la respuesta: candidates[0].content.parts[0].text
      JsonNode candidates = root.path("candidates");
      if (candidates.isArray() && candidates.size() > 0) {
        String text = candidates.get(0)
            .path("content")
            .path("parts")
            .get(0)
            .path("text")
            .asText();

        log.info("Gemini respondió exitosamente ({} caracteres)", text.length());
        return text;
      }

      log.warn("Gemini retornó respuesta sin candidatos. Usando fallback.");
      return fallback;

    } catch (Exception e) {
      log.error("Error al comunicarse con Gemini: {}. Usando fallback.", e.getMessage());
      return fallback;
    }
  }

  /**
   * Verifica si la API de Gemini está disponible y configurada.
   */
  public boolean isAvailable() {
    return apiKey != null && !apiKey.isBlank();
  }
}
