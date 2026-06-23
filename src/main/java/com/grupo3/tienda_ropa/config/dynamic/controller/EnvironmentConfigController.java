package com.grupo3.tienda_ropa.config.dynamic.controller;

import com.grupo3.tienda_ropa.config.dynamic.entity.Configuracion;
import com.grupo3.tienda_ropa.config.dynamic.service.DynamicConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/super-user/config")
public class EnvironmentConfigController {

    private final DynamicConfigService configService;

    // Lista de claves conocidas del proyecto para facilitar el control y visibilidad en el panel
    private static final List<Map<String, String>> CONTEXT_KEYS = List.of(
            Map.of("key", "INTERNAL_TOKEN", "fallback", "security.internal-token", "desc", "Token de seguridad para endpoints internos y reportes de backups."),
            Map.of("key", "JWT_SECRET_KEY", "fallback", "security.jwt.secret-key", "desc", "Clave secreta utilizada para firmar tokens JWT."),
            Map.of("key", "JWT_EXPIRATION_TIME", "fallback", "security.jwt.expiration-time", "desc", "Tiempo de expiración de los tokens JWT en milisegundos."),
            Map.of("key", "ADMIN_PASSCODE", "fallback", "security.admin-passcode", "desc", "Clave de acceso requerida para el registro de administradores."),
            Map.of("key", "MERCADOPAGO_ACCESS_TOKEN", "fallback", "mercadopago.access-token", "desc", "Access Token para integraciones con la API de Mercado Pago."),
            Map.of("key", "SUPABASE_URL", "fallback", "supabase.url", "desc", "URL del proyecto de Supabase Storage."),
            Map.of("key", "SUPABASE_SERVICE_ROLE_KEY", "fallback", "supabase.service-role-key", "desc", "Service role key para autenticación administrativa en Supabase."),
            Map.of("key", "MAIL_USERNAME", "fallback", "spring.mail.username", "desc", "Nombre de usuario o correo electrónico del servidor SMTP."),
            Map.of("key", "MAIL_PASSWORD", "fallback", "spring.mail.password", "desc", "Contraseña o token de aplicación para el servidor SMTP.")
    );

    public EnvironmentConfigController(DynamicConfigService configService) {
        this.configService = configService;
    }

    /**
     * Obtiene la lista de todas las variables de entorno configuradas o conocidas.
     * Los valores altamente sensibles se devuelven enmascarados.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllConfigs() {
        List<Map<String, Object>> responseList = new ArrayList<>();
        List<com.grupo3.tienda_ropa.config.dynamic.entity.Configuracion> dbConfigs = configService.getAllDbConfigs();
        
        for (Map<String, String> keyInfo : CONTEXT_KEYS) {
            String key = keyInfo.get("key");
            String fallback = keyInfo.get("fallback");
            String desc = keyInfo.get("desc");

            String rawVal = configService.getValue(key, fallback);
            String maskedVal = configService.maskSensitiveValue(key, rawVal);

            Map<String, Object> item = new HashMap<>();
            item.put("clave", key);
            item.put("valor", maskedVal);
            item.put("descripcion", desc);
            item.put("sobreescritoBd", dbConfigs.stream()
                    .anyMatch(c -> c.getClave().equalsIgnoreCase(key)));

            responseList.add(item);
        }

        // Agregar claves en la BD que no están en CONTEXT_KEYS
        for (com.grupo3.tienda_ropa.config.dynamic.entity.Configuracion dbConfig : dbConfigs) {
            boolean isKnown = CONTEXT_KEYS.stream()
                    .anyMatch(m -> m.get("key").equalsIgnoreCase(dbConfig.getClave()));
            if (!isKnown) {
                Map<String, Object> item = new HashMap<>();
                item.put("clave", dbConfig.getClave());
                item.put("valor", configService.maskSensitiveValue(dbConfig.getClave(), dbConfig.getValor()));
                item.put("descripcion", dbConfig.getDescripcion() != null ? dbConfig.getDescripcion() : "Configuración personalizada en Base de Datos");
                item.put("sobreescritoBd", true);
                responseList.add(item);
            }
        }

        return ResponseEntity.ok(responseList);
    }

    /**
     * Obtiene una configuración específica por su clave.
     * Permite ver el valor original sin enmascarar si se envía el parámetro unmasked=true.
     */
    @GetMapping("/{key}")
    public ResponseEntity<?> getConfig(
            @PathVariable String key,
            @RequestParam(defaultValue = "") String fallbackKey,
            @RequestParam(defaultValue = "false") boolean unmasked) {
        
        String actualFallback = fallbackKey;
        if (actualFallback.isEmpty()) {
            // Buscar si es una clave conocida
            actualFallback = CONTEXT_KEYS.stream()
                    .filter(m -> m.get("key").equalsIgnoreCase(key))
                    .findFirst()
                    .map(m -> m.get("fallback"))
                    .orElse(key);
        }

        String rawVal = configService.getValue(key, actualFallback);
        if (rawVal == null) {
            return ResponseEntity.notFound().build();
        }

        String displayVal = unmasked ? rawVal : configService.maskSensitiveValue(key, rawVal);

        return ResponseEntity.ok(Map.of(
                "clave", key,
                "valor", displayVal,
                "enmascarado", !unmasked
        ));
    }

    /**
     * Actualiza manualmente una configuración y genera un registro de auditoría.
     */
    @PutMapping("/{key}")
    public ResponseEntity<?> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String newValue = body.get("valor");
        String description = body.get("descripcion");

        if (newValue == null || newValue.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El valor no puede estar vacío"));
        }

        String actualDesc = description != null ? description : CONTEXT_KEYS.stream()
                .filter(m -> m.get("key").equalsIgnoreCase(key))
                .findFirst()
                .map(m -> m.get("desc"))
                .orElse("Actualizado vía panel de Superusuario");

        configService.updateValue(key, newValue, userDetails.getUsername(), actualDesc, "MODIFICAR");

        return ResponseEntity.ok(Map.of("mensaje", "Configuración '" + key + "' actualizada y auditada con éxito"));
    }

    /**
     * Regenera el token interno de forma segura, guardándolo en la base de datos
     * y retornando el nuevo token sin enmascarar una única vez.
     */
    @PostMapping("/regenerate-internal-token")
    public ResponseEntity<?> regenerateInternalToken(@AuthenticationPrincipal UserDetails userDetails) {
        String newToken = configService.generateSecureToken();
        
        configService.updateValue(
                "INTERNAL_TOKEN",
                newToken,
                userDetails.getUsername(),
                "Token interno regenerado automáticamente.",
                "REGENERAR"
        );

        return ResponseEntity.ok(Map.of(
                "mensaje", "Token interno regenerado con éxito.",
                "nuevoTokenInterno", newToken
        ));
    }

    /**
     * Retorna el listado de auditorías realizadas en el panel de configuración.
     */
    @GetMapping("/auditoria")
    public ResponseEntity<?> getAuditLogs() {
        return ResponseEntity.ok(configService.getAuditLogs());
    }

    /**
     * Elimina una configuración de la base de datos para restaurar su valor por defecto (fallback .env).
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<?> deleteConfig(
            @PathVariable String key,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boolean exists = configService.getAllDbConfigs().stream()
                .anyMatch(c -> c.getClave().equalsIgnoreCase(key));
        
        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        configService.deleteValue(key, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("mensaje", "Configuración '" + key + "' restablecida a su valor por defecto con éxito"));
    }
}
