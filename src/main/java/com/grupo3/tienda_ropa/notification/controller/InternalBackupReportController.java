package com.grupo3.tienda_ropa.notification.controller;

import com.grupo3.tienda_ropa.notification.dto.BackupReportDto;
import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import com.grupo3.tienda_ropa.notification.model.NotificationType;
import com.grupo3.tienda_ropa.notification.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/internal")
public class InternalBackupReportController {

    private final EmailNotificationService notificationService;

    @Value("${security.internal-token:urbanwear-secret-token-2026}")
    private String internalToken;

    @Value("${notification.admin-email:admin@urbanwear.com}")
    private String adminEmail;

    public InternalBackupReportController(EmailNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/backups/report")
    public ResponseEntity<String> handleBackupReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody BackupReportDto report) {

        // Validar token de seguridad compartido
        if (authHeader == null || !authHeader.equals("Bearer " + internalToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autorizado");
        }

        if ("FAILED".equalsIgnoreCase(report.getStatus())) {
            Map<String, Object> data = Map.of(
                "error", report.getError() != null ? report.getError() : "Error desconocido",
                "timestamp", LocalDateTime.now().toString()
            );

            notificationService.sendEmail(NotificationRequest.builder()
                .recipient(adminEmail)
                .subject("⚠️ Fallo en Backup de Base de Datos - UrbanWear")
                .type(NotificationType.BACKUP_ALERT)
                .templateData(data)
                .build());

            return ResponseEntity.ok("Alerta de fallo enviada al administrador.");
        }

        // Si es SUCCESS, podemos registrarlo o enviar un email informativo
        System.out.println("Backup exitoso recibido para el archivo: " + report.getFilename());
        return ResponseEntity.ok("Reporte de éxito procesado.");
    }
}
