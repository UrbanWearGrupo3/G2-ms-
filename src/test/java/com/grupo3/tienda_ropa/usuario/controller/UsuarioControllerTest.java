package com.grupo3.tienda_ropa.usuario.controller;

import com.grupo3.tienda_ropa.usuario.deto.*;
import com.grupo3.tienda_ropa.usuario.entity.Rol;
import com.grupo3.tienda_ropa.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private UsuarioController usuarioController;

    private RegistroRequest registroRequest;
    private UsuarioResponse usuarioResponse;
    private LoginRequest loginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        registroRequest = new RegistroRequest();
        registroRequest.setNombre("Juan");
        registroRequest.setApellido("Perez");
        registroRequest.setEmail("juan.perez@example.com");
        registroRequest.setPassword("secure123");

        usuarioResponse = new UsuarioResponse();
        usuarioResponse.setId(1L);
        usuarioResponse.setNombre("Juan");
        usuarioResponse.setApellido("Perez");
        usuarioResponse.setEmail("juan.perez@example.com");
        usuarioResponse.setRol("CLIENTE");
        usuarioResponse.setActivo(true);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan.perez@example.com");
        loginRequest.setPassword("secure123");

        loginResponse = new LoginResponse();
        loginResponse.setToken("mock-jwt-token");
        loginResponse.setEmail("juan.perez@example.com");
        loginResponse.setRol("CLIENTE");
        loginResponse.setNombre("Juan");
        loginResponse.setApellido("Perez");
    }

    @Test
    void testRegister() {
        when(usuarioService.register(any(RegistroRequest.class))).thenReturn(usuarioResponse);

        ResponseEntity<UsuarioResponse> response = authController.register(registroRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan", response.getBody().getNombre());
        verify(usuarioService, times(1)).register(any(RegistroRequest.class));
    }

    @Test
    void testLogin() {
        when(usuarioService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        verify(usuarioService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testGetProfile() {
        Principal principal = () -> "juan.perez@example.com";
        when(usuarioService.getProfile("juan.perez@example.com")).thenReturn(usuarioResponse);

        ResponseEntity<UsuarioResponse> response = usuarioController.getProfile(principal);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("juan.perez@example.com", response.getBody().getEmail());
        verify(usuarioService, times(1)).getProfile("juan.perez@example.com");
    }

    @Test
    void testUpdateProfile() {
        Principal principal = () -> "juan.perez@example.com";
        UsuarioUpdateRequest updateReq = new UsuarioUpdateRequest();
        updateReq.setNombre("Juan Carlos");
        updateReq.setApellido("Perez");

        UsuarioResponse updatedResponse = new UsuarioResponse();
        updatedResponse.setId(1L);
        updatedResponse.setNombre("Juan Carlos");
        updatedResponse.setApellido("Perez");
        updatedResponse.setEmail("juan.perez@example.com");
        updatedResponse.setRol("CLIENTE");
        updatedResponse.setActivo(true);

        when(usuarioService.updateProfile(eq("juan.perez@example.com"), any(UsuarioUpdateRequest.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<UsuarioResponse> response = usuarioController.updateProfile(principal, updateReq);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Carlos", response.getBody().getNombre());
        verify(usuarioService, times(1)).updateProfile(eq("juan.perez@example.com"), any(UsuarioUpdateRequest.class));
    }

    @Test
    void testFindAll() {
        Page<UsuarioResponse> page = new PageImpl<>(Collections.singletonList(usuarioResponse));
        when(usuarioService.findAll(any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<UsuarioResponse>> response = usuarioController.findAll(PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        verify(usuarioService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdateRol() {
        when(usuarioService.updateRol(eq(1L), eq(Rol.ADMIN))).thenReturn(usuarioResponse);

        ResponseEntity<UsuarioResponse> response = usuarioController.updateRol(1L, Rol.ADMIN);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(usuarioService, times(1)).updateRol(eq(1L), eq(Rol.ADMIN));
    }

    @Test
    void testDelete() {
        doNothing().when(usuarioService).delete(1L);

        ResponseEntity<Void> response = usuarioController.delete(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(usuarioService, times(1)).delete(1L);
    }
}
