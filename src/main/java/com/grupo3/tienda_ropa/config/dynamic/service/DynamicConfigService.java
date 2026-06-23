package com.grupo3.tienda_ropa.config.dynamic.service;

import com.grupo3.tienda_ropa.config.dynamic.entity.AuditoriaConfiguracion;
import com.grupo3.tienda_ropa.config.dynamic.entity.Configuracion;
import com.grupo3.tienda_ropa.config.dynamic.repository.AuditoriaConfiguracionRepository;
import com.grupo3.tienda_ropa.config.dynamic.repository.ConfiguracionRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class DynamicConfigService {

    private final ConfiguracionRepository configRepository;
    private final AuditoriaConfiguracionRepository auditRepository;
    private final Environment environment;

    public DynamicConfigService(ConfiguracionRepository configRepository, 
                                AuditoriaConfiguracionRepository auditRepository, 
                                Environment environment) {
        this.configRepository = configRepository;
        this.auditRepository = auditRepository;
        this.environment = environment;
    }

    /**
     * Obtiene el valor de una configuración. Primero busca en la base de datos y,
     * si no existe, busca en las variables de entorno / propiedades de Spring.
     * Este valor se almacena en caché.
     */
    @Cacheable(value = "configuraciones", key = "#key")
    public String getValue(String key, String envFallbackKey) {
        return configRepository.findById(key)
                .map(Configuracion::getValor)
                .orElseGet(() -> environment.getProperty(envFallbackKey));
    }

    /**
     * Actualiza o crea el valor de una configuración.
     * Desaloja el valor anterior de la caché de configuración.
     */
    @Transactional
    @CacheEvict(value = "configuraciones", key = "#key")
    public void updateValue(String key, String value, String updatedBy, String description, String actionType) {
        String previousValue = configRepository.findById(key)
                .map(Configuracion::getValor)
                .orElseGet(() -> environment.getProperty(key)); // Fallback a env si no estaba en BD

        Configuracion config = configRepository.findById(key)
                .orElse(new Configuracion());
        
        config.setClave(key);
        config.setValor(value);
        config.setDescripcion(description);
        config.setFechaActualizacion(LocalDateTime.now());
        config.setActualizadoPor(updatedBy);
        
        configRepository.save(config);

        // Registro de Auditoría
        AuditoriaConfiguracion audit = AuditoriaConfiguracion.builder()
                .clave(key)
                .valorAnterior(maskSensitiveValue(key, previousValue))
                .valorNuevo(maskSensitiveValue(key, value))
                .usuario(updatedBy)
                .tipoAccion(actionType)
                .fechaCambio(LocalDateTime.now())
                .build();
        
        auditRepository.save(audit);
    }

    /**
     * Genera un token hexadecimal seguro de 256 bits (64 caracteres).
     */
    public String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return HexFormat.of().formatHex(tokenBytes);
    }

    /**
     * Retorna todas las configuraciones persistidas en base de datos.
     */
    public List<Configuracion> getAllDbConfigs() {
        return configRepository.findAll();
    }

    /**
     * Retorna el historial de auditorías de configuración.
     */
    public List<AuditoriaConfiguracion> getAuditLogs() {
        return auditRepository.findAllByOrderByFechaCambioDesc();
    }

    /**
     * Elimina una configuración de la base de datos (restaurando el fallback de properties/env).
     * Desaloja el valor de la caché de configuración.
     */
    @Transactional
    @CacheEvict(value = "configuraciones", key = "#key")
    public void deleteValue(String key, String deletedBy) {
        String previousValue = configRepository.findById(key)
                .map(Configuracion::getValor)
                .orElse(null);

        configRepository.deleteById(key);

        // Registro de Auditoría para la eliminación
        AuditoriaConfiguracion audit = AuditoriaConfiguracion.builder()
                .clave(key)
                .valorAnterior(maskSensitiveValue(key, previousValue))
                .valorNuevo("[RESTABLECIDO A VALOR POR DEFECTO]")
                .usuario(deletedBy)
                .tipoAccion("RESTABLECER")
                .fechaCambio(LocalDateTime.now())
                .build();
        
        auditRepository.save(audit);
    }

    /**
     * Enmascara el valor si la clave contiene palabras clave que indiquen sensibilidad.
     */
    public String maskSensitiveValue(String key, String value) {
        if (value == null) {
            return null;
        }
        String upperKey = key.toUpperCase();
        boolean isSensitive = upperKey.contains("TOKEN") || 
                              upperKey.contains("KEY") || 
                              upperKey.contains("PASSWORD") || 
                              upperKey.contains("SECRET") || 
                              upperKey.contains("PASSCODE") || 
                              upperKey.contains("ACCESS");

        if (!isSensitive) {
            return value;
        }

        if (value.length() <= 8) {
            return "****";
        }

        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
