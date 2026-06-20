package com.grupo3.tienda_ropa.envio;

import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategy;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnvioStrategyContextTest {

    @Mock
    private EnvioStrategy correoArgentinoStrategy;

    @Mock
    private EnvioStrategy ocaStrategy;

    private EnvioStrategyContext context;

    @BeforeEach
    void setUp() {
        when(correoArgentinoStrategy.getProveedor()).thenReturn("CORREO_ARGENTINO");
        when(ocaStrategy.getProveedor()).thenReturn("OCA");
        context = new EnvioStrategyContext(List.of(correoArgentinoStrategy, ocaStrategy));
    }

    @Test
    void testGetStrategy_Success() {
        // Act
        EnvioStrategy resolved = context.getStrategy("CORREO_ARGENTINO");
        EnvioStrategy resolvedLowercase = context.getStrategy("correo_argentino");
        EnvioStrategy resolvedOca = context.getStrategy("OCA");

        // Assert
        assertEquals(correoArgentinoStrategy, resolved);
        assertEquals(correoArgentinoStrategy, resolvedLowercase);
        assertEquals(ocaStrategy, resolvedOca);
    }

    @Test
    void testGetStrategy_NotFound_ThrowsException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            context.getStrategy("UNKNOWN");
        });

        assertTrue(exception.getMessage().contains("No se encontró una estrategia de envío configurada"));
        assertTrue(exception.getMessage().contains("UNKNOWN"));
    }

    @Test
    void testGetStrategy_Null_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            context.getStrategy(null);
        });
    }

    @Test
    void testGetProveedoresSoportados() {
        // Act
        List<String> proveedores = context.getProveedoresSoportados();

        // Assert
        assertEquals(2, proveedores.size());
        assertTrue(proveedores.contains("CORREO_ARGENTINO"));
        assertTrue(proveedores.contains("OCA"));
    }
}
