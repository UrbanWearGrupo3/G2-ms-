package com.grupo3.tienda_ropa.config;

import com.grupo3.tienda_ropa.config.dynamic.entity.AuditoriaConfiguracion;
import com.grupo3.tienda_ropa.config.dynamic.entity.Configuracion;
import com.grupo3.tienda_ropa.config.dynamic.repository.AuditoriaConfiguracionRepository;
import com.grupo3.tienda_ropa.config.dynamic.repository.ConfiguracionRepository;
import com.grupo3.tienda_ropa.config.dynamic.service.DynamicConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DynamicConfigServiceTest {

    @Autowired
    private DynamicConfigService configService;

    @Autowired
    private ConfiguracionRepository configRepository;

    @Autowired
    private AuditoriaConfiguracionRepository auditRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        configRepository.deleteAll();
        auditRepository.deleteAll();
        // Limpiar la caché manualmente antes de cada test para asegurar que no interfiera
        if (cacheManager.getCache("configuraciones") != null) {
            cacheManager.getCache("configuraciones").clear();
        }
    }

    @Test
    void testGetValue_FromEnvironment_WhenNotInDb() {
        // Obtenemos un valor que no existe en DB, debe caer al fallback en application.properties de test
        String value = configService.getValue("INTERNAL_TOKEN", "security.internal-token");
        assertEquals("urbanwear-secret-token-2026", value);
    }

    @Test
    void testGetValue_FromDb_WhenOverridden() {
        // Guardamos una configuración directamente en base de datos
        Configuracion customConfig = Configuracion.builder()
                .clave("INTERNAL_TOKEN")
                .valor("token-personalizado-123456")
                .descripcion("Token personalizado")
                .build();
        configRepository.save(customConfig);

        String value = configService.getValue("INTERNAL_TOKEN", "security.internal-token");
        assertEquals("token-personalizado-123456", value);
    }

    @Test
    void testUpdateValue_ShouldSaveAndLogAudit() {
        // Actualizamos un valor
        configService.updateValue(
                "INTERNAL_TOKEN", 
                "token-actualizado-987654", 
                "superuser@urbanwear.com", 
                "Prueba de actualización", 
                "MODIFICAR"
        );

        // Verificar persistencia de la configuración
        String value = configService.getValue("INTERNAL_TOKEN", "security.internal-token");
        assertEquals("token-actualizado-987654", value);

        // Verificar persistencia de la auditoría
        List<AuditoriaConfiguracion> audits = auditRepository.findAllByOrderByFechaCambioDesc();
        assertFalse(audits.isEmpty());
        
        AuditoriaConfiguracion lastAudit = audits.getFirst();
        assertEquals("INTERNAL_TOKEN", lastAudit.getClave());
        assertEquals("superuser@urbanwear.com", lastAudit.getUsuario());
        assertEquals("MODIFICAR", lastAudit.getTipoAccion());
        
        // El valor nuevo debe estar enmascarado
        assertTrue(lastAudit.getValorNuevo().contains("****"));
    }

    @Test
    void testMaskSensitiveValue_ShouldMaskSensitiveKeysOnly() {
        // Clave sensible
        String sensitiveValue = "mi-password-secreto-super-seguro-123";
        String maskedSensitive = configService.maskSensitiveValue("JWT_SECRET_KEY", sensitiveValue);
        assertTrue(maskedSensitive.startsWith("mi-p"));
        assertTrue(maskedSensitive.endsWith("-123"));
        assertTrue(maskedSensitive.contains("****"));

        // Clave sensible corta
        String shortSensitive = "12345";
        String maskedShort = configService.maskSensitiveValue("ADMIN_PASSCODE", shortSensitive);
        assertEquals("****", maskedShort);

        // Clave no sensible
        String nonSensitiveValue = "https://mi-bucket.supabase.co";
        String maskedNonSensitive = configService.maskSensitiveValue("SUPABASE_URL", nonSensitiveValue);
        assertEquals(nonSensitiveValue, maskedNonSensitive);
    }

    @Test
    void testGenerateSecureToken_ShouldReturnValidHexToken() {
        String token = configService.generateSecureToken();
        assertNotNull(token);
        assertEquals(64, token.length()); // 32 bytes = 64 hex characters
    }
}
