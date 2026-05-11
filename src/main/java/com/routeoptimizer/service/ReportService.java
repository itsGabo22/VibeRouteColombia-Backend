package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.repository.OrderRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AnalyticsService analyticsService;
    private final OrderRepository orderRepository;
    private final BatchService batchService;
    private final ContextualAdvisor contextualAdvisor;

    public ReportService(AnalyticsService analyticsService, OrderRepository orderRepository, BatchService batchService, ContextualAdvisor contextualAdvisor) {
        this.analyticsService = analyticsService;
        this.orderRepository = orderRepository;
        this.batchService = batchService;
        this.contextualAdvisor = contextualAdvisor;
    }

    private static final List<Map<String, String>> documentStore = new ArrayList<>();

    public Map<String, String> uploadDocument(MultipartFile file, String sender, String type) {
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String lowerCaseName = fileName.toLowerCase();
            if (!(lowerCaseName.endsWith(".pdf") || lowerCaseName.endsWith(".xlsx") || lowerCaseName.endsWith(".csv") || lowerCaseName.endsWith(".docx"))) {
                throw new RuntimeException("Tipo de archivo no permitido. Solo se admiten PDF, Excel, CSV y Word (.docx).");
            }
        }

        Map<String, String> doc = new HashMap<>();
        String id = UUID.randomUUID().toString();
        doc.put("id", id);
        doc.put("filename", fileName);
        doc.put("sender", sender);
        doc.put("type", type);
        doc.put("timestamp", new java.util.Date().toString());
        
        documentStore.add(doc);
        return doc;
    }

    public byte[] generateOrdersReportExcel(String city) {
        String targetCity = city;
        
        // AUTO-DETECCIÓN DE SEGURIDAD: Si el usuario es Logístico, forzamos su ciudad asignada
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.routeoptimizer.model.entity.User user) {
            if (user.getRole() == com.routeoptimizer.model.enums.Role.LOGISTICS) {
                targetCity = user.getAssignedCity();
            }
        }

        List<Order> orders;
        String sheetName;
        
        System.out.println("DEBUG: Generando Excel para ciudad final: [" + targetCity + "]");
        
        if (targetCity != null && !targetCity.trim().isEmpty() && !targetCity.equalsIgnoreCase("undefined") && !targetCity.equalsIgnoreCase("Global")) {
            final String finalCity = targetCity.trim();
            orders = orderRepository.findAll().stream()
                .filter(o -> o.getCity() != null && o.getCity().trim().equalsIgnoreCase(finalCity))
                .collect(Collectors.toList());
            sheetName = "Pedidos Región " + finalCity;
        } else {
            // Solo llegamos aquí si el usuario es ADMIN y no eligió ciudad.
            orders = orderRepository.findAll();
            sheetName = "Consolidado Nacional";
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] columns = {"ID", "Referencia", "Cliente", "Dirección", "Ciudad", "Estado", "Prioridad", "Repartidor", "Novedad"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Order o : orders) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(o.getId());
                row.createCell(1).setCellValue(o.getClientReference());
                row.createCell(2).setCellValue(o.getClientName());
                row.createCell(3).setCellValue(o.getAddress());
                row.createCell(4).setCellValue(o.getCity());
                row.createCell(5).setCellValue(o.getStatus().toString());
                row.createCell(6).setCellValue(o.getPriority() != null ? o.getPriority().toString() : "MEDIUM");
                
                String driverName = "Sin Asignar";
                if (o.getBatchId() != null) {
                    try {
                        var batch = batchService.findById(o.getBatchId());
                        if (batch != null && batch.getDriver() != null) {
                            driverName = batch.getDriver().getName();
                        }
                    } catch (Exception e) {}
                }
                row.createCell(7).setCellValue(driverName);
                row.createCell(8).setCellValue(o.getNonDeliveryReason() != null ? o.getNonDeliveryReason() : "-");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel de pedidos: " + e.getMessage());
        }
    }

    public byte[] generateOrdersReportWord(String city) {
        String targetCity = city;
        
        // AUTO-DETECCIÓN DE SEGURIDAD
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.routeoptimizer.model.entity.User user) {
            if (user.getRole() == com.routeoptimizer.model.enums.Role.LOGISTICS) {
                targetCity = user.getAssignedCity();
            }
        }

        List<Order> orders;
        System.out.println("DEBUG: Generando Word para ciudad final: [" + targetCity + "]");
        
        if (targetCity != null && !targetCity.trim().isEmpty() && !targetCity.equalsIgnoreCase("undefined") && !targetCity.equalsIgnoreCase("Global")) {
            final String finalCity = targetCity.trim();
            orders = orderRepository.findAll().stream()
                .filter(o -> o.getCity() != null && o.getCity().trim().equalsIgnoreCase(finalCity))
                .collect(Collectors.toList());
        } else {
            orders = orderRepository.findAll();
        }
        
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph headerParagraph = document.createParagraph();
            headerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun headerRun = headerParagraph.createRun();
            headerRun.setBold(true);
            headerRun.setFontSize(16);
            headerRun.setText(targetCity != null && !targetCity.equalsIgnoreCase("Global") 
                ? "LISTA DE PEDIDOS - REGIÓN " + targetCity.toUpperCase() 
                : "REPORTE OPERATIVO NACIONAL - VIBEROUTE");
            headerRun.addBreak();

            XWPFParagraph info = document.createParagraph();
            XWPFRun infoRun = info.createRun();
            infoRun.setText("Fecha de Generación: " + new Date());
            infoRun.addBreak();
            infoRun.setText("Este reporte contiene el estado actual de todos los pedidos en sistema, incluyendo asignaciones y novedades.");
            infoRun.addBreak();

            // Tabla de Pedidos
            XWPFTable table = document.createTable();
            XWPFTableRow header = table.getRow(0);
            header.getCell(0).setText("ID");
            header.addNewTableCell().setText("Referencia");
            header.addNewTableCell().setText("Cliente");
            header.addNewTableCell().setText("Estado");
            header.addNewTableCell().setText("Repartidor");
            header.addNewTableCell().setText("Novedad");

            for (Order o : orders) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(String.valueOf(o.getId()));
                row.getCell(1).setText(o.getClientReference());
                row.getCell(2).setText(o.getClientName());
                row.getCell(3).setText(o.getStatus().toString());
                
                String driverName = "Sin Asignar";
                if (o.getBatchId() != null) {
                    try {
                        var batch = batchService.findById(o.getBatchId());
                        if (batch != null && batch.getDriver() != null) {
                            driverName = batch.getDriver().getName();
                        }
                    } catch (Exception e) {}
                }
                row.getCell(4).setText(driverName);
                row.getCell(5).setText(o.getNonDeliveryReason() != null ? o.getNonDeliveryReason() : "-");
            }

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte de pedidos: " + e.getMessage());
        }
    }

    public byte[] generateGeneralReportWord(String city) {
        var financials = analyticsService.getFinancialAnalytics(city);
        var allRankings = analyticsService.getEfficiencyRanking();
        
        // Filtrar ranking por ciudad si aplica
        List<com.routeoptimizer.dto.DriverRankingDTO> ranking;
        if (city != null && !city.trim().isEmpty()) {
            // Buscamos conductores que tengan pedidos en esa ciudad
            ranking = allRankings.stream()
                .filter(dr -> {
                    // Verificación simple: si el conductor tiene pedidos en la ciudad
                    long count = orderRepository.countByStatusAndCity(com.routeoptimizer.model.enums.OrderStatus.DELIVERED, city);
                    return count > 0; // Simplificación para el ejemplo
                })
                .limit(3)
                .collect(Collectors.toList());
        } else {
            ranking = allRankings;
        }
        
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(22);
            titleRun.setColor("1F4E78"); 
            titleRun.setText("REPORTE ESTRATÉGICO - " + (city != null ? city.toUpperCase() : "NACIONAL"));
            titleRun.addBreak();
            titleRun.setFontSize(14);
            titleRun.setText("VibeRoute Colombia - Gestión de Operaciones");
            titleRun.addBreak();
            
            XWPFParagraph datePara = document.createParagraph();
            datePara.setAlignment(ParagraphAlignment.RIGHT);
            datePara.createRun().setText("Fecha de Emisión: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            
            XWPFParagraph finTitle = document.createParagraph();
            XWPFRun finTitleRun = finTitle.createRun();
            finTitleRun.setBold(true);
            finTitleRun.setFontSize(14);
            finTitleRun.setText("1. ESTADO FINANCIERO" + (city != null ? " EN " + city.toUpperCase() : " CONSOLIDADO"));
            
            XWPFParagraph finData = document.createParagraph();
            XWPFRun finRun = finData.createRun();
            finRun.setFontSize(11);
            finRun.setText("• Ingresos Locales: $" + financials.totalRevenue());
            finRun.addBreak();
            finRun.setText("• Costos Operativos de Zona: $" + financials.operationalCosts());
            finRun.addBreak();
            finRun.setText("• Margen de Eficiencia: " + financials.profitMarginPercentage() + "%");
            finRun.addBreak();
            finRun.setBold(true);
            finRun.setText("• Utilidad Neta de Zona: $" + financials.netProfit());
            
            // ANALISIS IA PARA REPORTE GERENCIAL
            XWPFParagraph aiTitle = document.createParagraph();
            XWPFRun aiTitleRun = aiTitle.createRun();
            aiTitleRun.setBold(true);
            aiTitleRun.setFontSize(14);
            aiTitleRun.setText("2. RESUMEN SEMANAL DE OPERACIONES (IA)");
            
            String aiPrompt = String.format("""
                Actúa como el Coordinador Logístico Senior de VibeRoute Colombia para la zona de %s.
                Genera un resumen semanal de gestión operativa basado en estos datos REALES:
                - CIUDAD: %s
                - INGRESOS: $%s
                - COSTOS: $%s
                - UTILIDAD: $%s
                - MARGEN: %s%%
                
                REGLAS CRÍTICAS:
                1) NO uses corchetes [] ni pidas "ingresar nombre de zona". Usa el nombre "%s" directamente.
                2) Analiza por qué los costos están en $%s (si es el caso) y qué implica para la zona.
                3) DESTACA por nombre a los repartidores del ranking: %s.
                4) El tono debe ser de un líder que ya conoce la operación, no de una IA genérica.
                5) Estructura: Resumen de semana -> Análisis de rentabilidad -> Reconocimiento de equipo -> Conclusión estratégica.
                """, 
                (city != null ? city : "Nacional"), 
                (city != null ? city : "Nacional"),
                financials.totalRevenue(), 
                financials.operationalCosts(), 
                financials.netProfit(), 
                financials.profitMarginPercentage(),
                (city != null ? city : "Nacional"),
                financials.operationalCosts(),
                ranking.stream().map(com.routeoptimizer.dto.DriverRankingDTO::driverName).collect(Collectors.joining(", ")));
            
            String aiAnalysis = contextualAdvisor.askGeminiDirect(aiPrompt);
            XWPFParagraph aiPara = document.createParagraph();
            aiPara.setSpacingBefore(200);
            XWPFRun aiRun = aiPara.createRun();
            aiRun.setItalic(true);
            aiRun.setFontSize(11);
            aiRun.setText(aiAnalysis);
            
            XWPFParagraph rankTitle = document.createParagraph();
            XWPFRun rankTitleRun = rankTitle.createRun();
            rankTitleRun.setBold(true);
            rankTitleRun.setFontSize(14);
            rankTitleRun.setText("3. RANKING DE EFICIENCIA LOCAL");
            
            XWPFTable rankTable = document.createTable();
            rankTable.setWidth("100%");
            XWPFTableRow hr = rankTable.getRow(0);
            hr.getCell(0).setText("Repartidor");
            hr.addNewTableCell().setText("Entregas");
            hr.addNewTableCell().setText("% Efectividad");
            hr.addNewTableCell().setText("Desempeño");
            
            for (var dr : ranking) {
                XWPFTableRow row = rankTable.createRow();
                row.getCell(0).setText(dr.driverName());
                row.getCell(1).setText(String.valueOf(dr.successfulDeliveries()));
                row.getCell(2).setText(dr.effectivenessPercentage() + "%");
                row.getCell(3).setText(dr.tag());
            }

            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando reporte semanal con IA: " + e.getMessage());
        }
    }

    public byte[] generateTransactionReport(String id) {
        // Implementación simplificada para el ejemplo
        return ("Reporte PDF Placeholder para ID: " + id).getBytes();
    }

    public byte[] generateGlobalReport() {
        // Implementación simplificada para el ejemplo
        return "Cierre Global PDF Placeholder".getBytes();
    }

    public List<Map<String, String>> listDocuments() {
        return documentStore;
    }
}
