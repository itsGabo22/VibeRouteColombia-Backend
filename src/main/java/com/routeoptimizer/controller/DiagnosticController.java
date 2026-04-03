package com.routeoptimizer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class DiagnosticController {

  @GetMapping("/api/v1/ping")
  public Map<String, String> ping() {
    return Map.of("status", "UP", "package", "com.routeoptimizer");
  }
}
