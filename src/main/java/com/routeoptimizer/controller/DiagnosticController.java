package com.routeoptimizer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/diagnostic")
public class DiagnosticController {

  @Value("${google.maps.api.key:}")
  private String googleMapsKey;

  @Value("${gemini.api.key:}")
  private String geminiKey;

  @GetMapping("/ping")
  public Map<String, String> ping() {
    return Map.of("status", "UP", "package", "com.routeoptimizer");
  }

  @GetMapping("/keys")
  public Map<String, Object> checkKeys() {
    return Map.of(
        "googleMapsLoaded", (googleMapsKey != null && !googleMapsKey.isEmpty()),
        "googleMapsMasked", mask(googleMapsKey),
        "geminiLoaded", (geminiKey != null && !geminiKey.isEmpty()),
        "geminiMasked", mask(geminiKey),
        "info", "Las llaves se cargan desde el archivo .env usando DotenvInitializer"
    );
  }

  private String mask(String key) {
    if (key == null || key.length() < 8)
      return "VACÍA O NO DETECTADA";
    return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
  }
}
