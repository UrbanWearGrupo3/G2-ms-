package com.grupo3.tienda_ropa.Pedidos.service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.repository.DetallePedidosRepository;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import com.grupo3.tienda_ropa.notification.service.EmailNotificationService;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepo carritoItemRepo;

    @Mock
    private PedidosRepository pedidosRepository;

    @Mock
    private DetallePedidosRepository detallePedidosRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                carritoRepository,
                carritoItemRepo,
                pedidosRepository,
                detallePedidosRepository,
                emailNotificationService
        );
    }

    @Test
    void testConfirmarPedido_Success() {
        // Arrange
        Long usuarioId = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombre("Gerardo");
        usuario.setApellido("Vega");
        usuario.setEmail("gerardo@tienda.com");

        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(10L);
        carrito.setUsuario(usuario);

        Producto producto = new Producto();
        producto.setId(100L);
        producto.setNombre("Remera Urban");
        producto.setPrecio(BigDecimal.valueOf(1500));

        CarritoItem item = new CarritoItem();
        item.setId(200L);
        item.setCarrito(carrito);
        item.setProducto(producto);
        item.setCantidad(2);

        Pedido mockPedidoGuardado = new Pedido();
        mockPedidoGuardado.setId(500L);
        mockPedidoGuardado.setUsuario(usuario);
        mockPedidoGuardado.setTotal(BigDecimal.valueOf(3000));

        when(carritoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId())).thenReturn(List.of(item));
        when(pedidosRepository.save(any(Pedido.class))).thenReturn(mockPedidoGuardado);

        // Act
        Pedido result = pedidoService.confirmarPedido(usuarioId);

        // Assert
        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals(BigDecimal.valueOf(3000), result.getTotal());
        
        verify(carritoRepository, times(1)).findByUsuario_Id(usuarioId);
        verify(carritoItemRepo, times(1)).findByCarritoId(carrito.getId());
        verify(pedidosRepository, times(1)).save(any(Pedido.class));
        verify(detallePedidosRepository, times(1)).saveAll(anyList());
        verify(carritoItemRepo, times(1)).deleteAll(anyList());
        verify(emailNotificationService, times(1)).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void testConfirmarPedido_EmptyCart_ShouldThrowException() {
        // Arrange
        Long usuarioId = 2L;
        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(11L);

        when(carritoRepository.findByUsuario_Id(usuarioId)).thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId())).thenReturn(Collections.emptyList());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            pedidoService.confirmarPedido(usuarioId)
        );
        assertEquals("El carrito está vacío", exception.getMessage());
        
        verify(pedidosRepository, never()).save(any(Pedido.class));
        verify(emailNotificationService, never()).sendEmail(any(NotificationRequest.class));
    }

    @Test
    void testActualizarEstado_Success() {
        // Arrange
        Long pedidoId = 500L;
        Usuario usuario = new Usuario();
        usuario.setNombre("Gerardo");
        usuario.setApellido("Vega");
        usuario.setEmail("gerardo@tienda.com");

        Pedido pedido = new Pedido();
        pedido.setId(pedidoId);
        pedido.setUsuario(usuario);
        pedido.setEstado("PENDIENTE");
        pedido.setDetalles(Collections.emptyList());
        pedido.setTotal(BigDecimal.ZERO);

        when(pedidosRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidosRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Pedido result = pedidoService.actualizarEstado(pedidoId, "APROBADO");

        // Assert
        assertNotNull(result);
        assertEquals("APROBADO", result.getEstado());
        verify(pedidosRepository, times(1)).findById(pedidoId);
        verify(pedidosRepository, times(1)).save(pedido);
        verify(emailNotificationService, times(1)).sendEmail(any(NotificationRequest.class));
    }
}
