package com.grupo3.tienda_ropa.carrito.service;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private UsuarioService usuarioService;

    private CarritoService carritoService;

    @BeforeEach
    void setUp() {
        carritoService = new CarritoService(carritoRepository, usuarioService);
    }

    @Test
    void testObtenerOCrearCarrito_WhenExists_ShouldReturnExisting() {
        // Arrange
        Long usuarioId = 1L;
        CarritoEntity existingCarrito = new CarritoEntity();
        existingCarrito.setId(10L);
        
        when(carritoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(existingCarrito));

        // Act
        CarritoEntity result = carritoService.obtenerOCrearCarrito(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getId());
        verify(carritoRepository, times(1)).findByUsuario_Id(usuarioId);
        verify(usuarioService, never()).obtenerUsuarioPorId(anyLong());
        verify(carritoRepository, never()).save(any(CarritoEntity.class));
    }

    @Test
    void testObtenerOCrearCarrito_WhenNotExists_ShouldCreateAndSave() {
        // Arrange
        Long usuarioId = 2L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        
        CarritoEntity savedCarrito = new CarritoEntity();
        savedCarrito.setId(20L);
        savedCarrito.setUsuario(usuario);

        when(carritoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.empty());
        when(usuarioService.obtenerUsuarioPorId(usuarioId)).thenReturn(usuario);
        when(carritoRepository.save(any(CarritoEntity.class))).thenReturn(savedCarrito);

        // Act
        CarritoEntity result = carritoService.obtenerOCrearCarrito(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals(usuarioId, result.getUsuario().getId());
        verify(carritoRepository, times(1)).findByUsuario_Id(usuarioId);
        verify(usuarioService, times(1)).obtenerUsuarioPorId(usuarioId);
        verify(carritoRepository, times(1)).save(any(CarritoEntity.class));
    }

    @Test
    void testObtenerOCrearCarritoPorEmail_Success() {
        // Arrange
        String email = "cliente@tienda.com";
        Long usuarioId = 3L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEmail(email);

        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(30L);
        carrito.setUsuario(usuario);

        when(usuarioService.obtenerUsuarioPorEmail(email)).thenReturn(usuario);
        when(carritoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(carrito));

        // Act
        CarritoEntity result = carritoService.obtenerOCrearCarritoPorEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(30L, result.getId());
        verify(usuarioService, times(1)).obtenerUsuarioPorEmail(email);
        verify(carritoRepository, times(1)).findByUsuario_Id(usuarioId);
    }
}
