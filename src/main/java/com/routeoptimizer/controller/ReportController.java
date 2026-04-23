package com.routeoptimizer.controller;

import com.routeoptimizer.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sender") String sender,
            @RequestParam("type") String type) {
        
        return ResponseEntity.ok(reportService.uploadDocument(file, sender, type));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
        byte[] pdfBytes = reportService.generateTransactionReport(id);
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"reporte_" + id + ".pdf\"")
                .body(pdfBytes);
    }

    @GetMapping("/generate-global")
    public ResponseEntity<byte[]> generateGlobalReport() {
        byte[] pdfBytes = reportService.generateGlobalReport();
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"Cierre_Global_VibeRoute.pdf\"")
                .body(pdfBytes);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listDocuments() {
        return ResponseEntity.ok(reportService.listDocuments());
    }
}

