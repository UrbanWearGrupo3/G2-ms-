package com.grupo3.tienda_ropa.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    // Clave secreta válida de 256 bits codificada en Base64 para testing
    private final String base64Secret = "NDA0RTYzNTI2NjU1NkE1ODZOMzI3MjM1NzUzODd4MkY0MTNGNDQyODQ3MkI0QjYyNTA2NDUzNjc1NjZCNTk3MA==";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
        jwtService.init();
    }

    @Test
    void testInit_WithEmptySecretKey_ShouldGenerateRandomKey() {
        JwtService serviceWithEmptyKey = new JwtService();
        ReflectionTestUtils.setField(serviceWithEmptyKey, "secretKey", "");
        ReflectionTestUtils.setField(serviceWithEmptyKey, "jwtExpiration", 86400000L);
        
        assertDoesNotThrow(serviceWithEmptyKey::init);
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        // Arrange
        String username = "test@urbanwear.com";
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        
        when(userDetails.getUsername()).thenReturn(username);
        // Utilizar cast explícito para Mockito
        when(userDetails.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act
        String token = jwtService.generateToken(userDetails);
        
        // Assert
        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testExtractClaim_Success() {
        // Arrange
        String username = "user@urbanwear.com";
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"));
        
        when(userDetails.getUsername()).thenReturn(username);
        when(userDetails.getAuthorities()).thenAnswer(invocation -> authorities);

        // Act
        String token = jwtService.generateToken(userDetails);
        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());
        String rol = jwtService.extractClaim(token, claims -> claims.get("rol", String.class));

        // Assert
        assertEquals(username, subject);
        assertEquals("CLIENTE", rol);
    }
}
