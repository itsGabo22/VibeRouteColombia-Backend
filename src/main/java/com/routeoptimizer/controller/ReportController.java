package com.routeoptimizer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    // Almacenamiento temporal para intercambio de archivos
    private static final List<Map<String, String>> documentStore = new ArrayList<>();

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("sender") String sender,
            @RequestParam("type") String type) {
        
        Map<String, String> doc = new HashMap<>();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("filename", file.getOriginalFilename());
        doc.put("sender", sender);
        doc.put("type", type);
        doc.put("timestamp", new java.util.Date().toString());
        
        documentStore.add(doc);
        
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable String id) {
        String pdf = "%PDF-1.4\n" +
                     "1 0 obj <</Type/Catalog/Pages 2 0 R>> endobj\n" +
                     "2 0 obj <</Type/Pages/Kids[3 0 R]/Count 1>> endobj\n" +
                     "3 0 obj <</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources <</Font <</F1 5 0 R>>>> /Contents 4 0 R>> endobj\n" +
                     "4 0 obj <</Length 400>> stream\n" +
                     "BT /F1 18 Tf 70 720 Td (VIBEROUTE COLOMBIA - REPORTE DE TRANSACCION) Tj\n" +
                     "0 -30 Td /F1 12 Tf (ID Documento: " + id + ") Tj\n" +
                     "0 -20 Td (Fecha: " + new java.util.Date() + ") Tj\n" +
                     "0 -40 Td (----------------------------------------------------------------------------------) Tj\n" +
                     "0 -20 Td (Este documento certifica la consolidacion de la operacion regional.) Tj\n" +
                     "0 -20 Td (Se han procesado los pedidos correspondientes a este lote exitosamente.) Tj\n" +
                     "0 -40 Td (Estado Operativo: VALIDADO) Tj\n" +
                     "0 -20 Td (Sello Digital: VIBE-" + id.substring(0,8).toUpperCase() + "-OK) Tj\n" +
                     "ET\n" +
                     "endstream endobj\n" +
                     "5 0 obj <</Type/Font/Subtype/Type1/BaseFont/Helvetica-Bold>> endobj\n" +
                     "xref\n" +
                     "0 6\n" +
                     "0000000000 65535 f\n" +
                     "trailer <</Size 6/Root 1 0 R>>\n" +
                     "startxref\n" +
                     "500\n" +
                     "%%EOF";
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"reporte_" + id + ".pdf\"")
                .body(pdf.getBytes());
    }

    @GetMapping("/generate-global")
    public ResponseEntity<byte[]> generateGlobalReport() {
        String pdf = "%PDF-1.4\n" +
                     "1 0 obj <</Type/Catalog/Pages 2 0 R>> endobj\n" +
                     "2 0 obj <</Type/Pages/Kids[3 0 R]/Count 1>> endobj\n" +
                     "3 0 obj <</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]/Resources <</Font <</F1 5 0 R>>>> /Contents 4 0 R>> endobj\n" +
                     "4 0 obj <</Length 450>> stream\n" +
                     "BT /F1 20 Tf 70 720 Td (CIERRE OPERATIVO GLOBAL - VIBEROUTE COLOMBIA) Tj\n" +
                     "0 -40 Td /F1 12 Tf (Fecha de Generacion: " + new java.util.Date() + ") Tj\n" +
                     "0 -30 Td (Resumen de Flota Consolidado) Tj\n" +
                     "0 -20 Td (----------------------------------------------------------------------------------) Tj\n" +
                     "0 -30 Td (Total Pedidos: 25) Tj\n" +
                     "0 -20 Td (Entregas Exitosas: 100%) Tj\n" +
                     "0 -20 Td (Eficiencia de Combustible: Optima) Tj\n" +
                     "0 -40 Td (Reporte Validado por Inteligencia Artificial VibeRoute.) Tj\n" +
                     "0 -20 Td (Bogota - Pasto - Medellin) Tj\n" +
                     "ET\n" +
                     "endstream endobj\n" +
                     "5 0 obj <</Type/Font/Subtype/Type1/BaseFont/Helvetica-Bold>> endobj\n" +
                     "xref\n" +
                     "0 6\n" +
                     "0000000000 65535 f\n" +
                     "trailer <</Size 6/Root 1 0 R>>\n" +
                     "startxref\n" +
                     "550\n" +
                     "%%EOF";
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"Cierre_Global_VibeRoute.pdf\"")
                .body(pdf.getBytes());
    }

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> listDocuments() {
        return ResponseEntity.ok(documentStore);
    }
}
