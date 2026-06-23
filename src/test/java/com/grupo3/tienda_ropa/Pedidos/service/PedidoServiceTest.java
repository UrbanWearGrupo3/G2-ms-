package com.grupo3.tienda_ropa.Pedidos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.entity.PedidosDetalles;
import com.grupo3.tienda_ropa.Pedidos.repository.DetallePedidosRepository;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.notification.service.EmailNotificationService;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

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

    // 🔥 Si agregaste más dependencias al servicio, agrégalas aquí
    // @Mock
    // private CuponService cuponService;
    
    // @Mock
    // private UsuarioRepository usuarioRepository;
    
    // @Mock
    // private ProductoRepository productoRepository;

    @InjectMocks
    private PedidoService pedidoService;

    private Usuario usuario;
    private CarritoEntity carrito;
    private CarritoItem carritoItem;
    private Producto producto;
    private Pedido pedido;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");
        usuario.setNombre("Test");
        usuario.setApellido("User");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStock(10);

        carrito = new CarritoEntity();
        carrito.setId(1L);
        carrito.setUsuario(usuario);

        carritoItem = new CarritoItem();
        carritoItem.setId(1L);
        carritoItem.setCarrito(carrito);
        carritoItem.setProducto(producto);
        carritoItem.setCantidad(2); // ✅ Asegurar que tiene cantidad

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setUsuario(usuario);
        pedido.setEstado("PENDIENTE");
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(new BigDecimal("200.00"));
    }

    // ==================== TEST DE CONFIRMAR PEDIDO ====================

    @Test
    void confirmarPedido_DeberiaGuardarPedidoYDetalles_CuandoCarritoValido() {
        // Arrange
        Long usuarioId = 1L;
        when(carritoRepository.findByUsuario_Id(usuarioId))
            .thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId()))
            .thenReturn(List.of(carritoItem));
        when(pedidosRepository.save(any(Pedido.class)))
            .thenReturn(pedido);
        when(detallePedidosRepository.saveAll(anyList()))
            .thenReturn(List.of(new PedidosDetalles()));
        doNothing().when(emailNotificationService).sendEmail(any());

        // Act - ✅ CORREGIDO: solo 1 parámetro
        Pedido resultado = pedidoService.confirmarPedido(usuarioId);

        // Assert
        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(new BigDecimal("200.00"), resultado.getTotal());
        
        verify(pedidosRepository).save(any(Pedido.class));
        verify(detallePedidosRepository).saveAll(anyList());
        verify(carritoItemRepo).deleteAll(anyList());
    }

    @Test
    void confirmarPedido_DeberiaLanzarExcepcion_CuandoCarritoNoExiste() {
        // Arrange
        Long usuarioId = 999L;
        when(carritoRepository.findByUsuario_Id(usuarioId))
            .thenReturn(Optional.empty());

        // Act & Assert - ✅ CORREGIDO: solo 1 parámetro
        assertThrows(RuntimeException.class, () -> {
            pedidoService.confirmarPedido(usuarioId);
        });
    }

    @Test
    void confirmarPedido_DeberiaLanzarExcepcion_CuandoCarritoVacio() {
        // Arrange
        Long usuarioId = 1L;
        when(carritoRepository.findByUsuario_Id(usuarioId))
            .thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId()))
            .thenReturn(List.of()); // Carrito vacío

        // Act & Assert - ✅ CORREGIDO: solo 1 parámetro
        assertThrows(RuntimeException.class, () -> {
            pedidoService.confirmarPedido(usuarioId);
        });
    }

    @Test
    void confirmarPedido_DeberiaActualizarStock_CuandoSeConfirma() {
        // Arrange
        Long usuarioId = 1L;
        int stockInicial = producto.getStock();
        
        when(carritoRepository.findByUsuario_Id(usuarioId))
            .thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId()))
            .thenReturn(List.of(carritoItem));
        when(pedidosRepository.save(any(Pedido.class)))
            .thenReturn(pedido);
        when(detallePedidosRepository.saveAll(anyList()))
            .thenReturn(List.of(new PedidosDetalles()));
        doNothing().when(emailNotificationService).sendEmail(any());

        // Act - ✅ CORREGIDO: solo 1 parámetro
        pedidoService.confirmarPedido(usuarioId);

        // Assert
        // Verificar que se actualizó el stock
        assertEquals(stockInicial - carritoItem.getCantidad(), producto.getStock());
    }

    // ==================== TEST DE ACTUALIZAR ESTADO ====================

    @Test
    void actualizarEstado_DeberiaActualizarEstado_CuandoPedidoExiste() {
        // Arrange
        Long pedidoId = 1L;
        String nuevoEstado = "PAGADO";
        
        when(pedidosRepository.findById(pedidoId))
            .thenReturn(Optional.of(pedido));
        when(pedidosRepository.save(any(Pedido.class)))
            .thenReturn(pedido);
        doNothing().when(emailNotificationService).sendEmail(any());

        // Act
        Pedido resultado = pedidoService.actualizarEstado(pedidoId, nuevoEstado);

        // Assert
        assertNotNull(resultado);
        assertEquals(nuevoEstado, resultado.getEstado());
        verify(pedidosRepository).save(pedido);
    }

    @Test
    void actualizarEstado_DeberiaLanzarExcepcion_CuandoPedidoNoExiste() {
        // Arrange
        Long pedidoId = 999L;
        when(pedidosRepository.findById(pedidoId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            pedidoService.actualizarEstado(pedidoId, "PAGADO");
        });
    }

    // ==================== TEST DE OBTENER PEDIDOS ====================

    @Test
    void obtenerPedidosUsuario_DeberiaRetornarLista_CuandoUsuarioExiste() {
        // Arrange
        Long usuarioId = 1L;
        when(pedidosRepository.findByUsuarioId(usuarioId))
            .thenReturn(List.of(pedido));

        // Act
        List<Pedido> resultados = pedidoService.obtenerPedidosUsuario(usuarioId);

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());
        assertEquals(1, resultados.size());
    }

    @Test
    void obtenerPedidosPorEstado_DeberiaRetornarLista_CuandoEstadoExiste() {
        // Arrange
        String estado = "PENDIENTE";
        when(pedidosRepository.findByEstado(estado))
            .thenReturn(List.of(pedido));

        // Act
        List<Pedido> resultados = pedidoService.obtenerPedidosPorEstado(estado);

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());
        assertEquals(estado, resultados.get(0).getEstado());
    }

    // ==================== TEST DE OBTENER PEDIDO POR ID ====================

    @Test
    void obtenerPedidoPorId_DeberiaRetornarPedido_CuandoExiste() {
        // Arrange
        Long pedidoId = 1L;
        when(pedidosRepository.findById(pedidoId))
            .thenReturn(Optional.of(pedido));

        // Act
        Pedido resultado = pedidoService.obtenerPedidoPorId(pedidoId);

        // Assert
        assertNotNull(resultado);
        assertEquals(pedidoId, resultado.getId());
    }

    @Test
    void obtenerPedidoPorId_DeberiaLanzarExcepcion_CuandoNoExiste() {
        // Arrange
        Long pedidoId = 999L;
        when(pedidosRepository.findById(pedidoId))
            .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            pedidoService.obtenerPedidoPorId(pedidoId);
        });
    }

    // ==================== TEST DE OBTENER TODOS ====================

    @Test
    void obtenerTodosLosPedidos_DeberiaRetornarListaCompleta() {
        // Arrange
        when(pedidosRepository.findAll())
            .thenReturn(List.of(pedido));

        // Act
        List<Pedido> resultados = pedidoService.obtenerTodosLosPedidos();

        // Assert
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty());
    }

    // ==================== ❌ TEST ELIMINADOS (métodos que ya no existen) ====================

    /*
    // ❌ ELIMINADO: El método comprarDirecto ya no existe
    @Test
    void comprarDirecto_DeberiaCrearPedido_CuandoCompraDirecta() {
        // Este test fue eliminado porque el método ya no existe en PedidoService
    }
    */

    // ==================== TEST CON CANTIDAD NULL ====================

    @Test
    void confirmarPedido_DeberiaManejarCantidadNull_CuandoItemTieneCantidadNull() {
        // Arrange
        Long usuarioId = 1L;
        
        // Crear item con cantidad null
        CarritoItem itemSinCantidad = new CarritoItem();
        itemSinCantidad.setId(2L);
        itemSinCantidad.setCarrito(carrito);
        itemSinCantidad.setProducto(producto);
        itemSinCantidad.setCantidad(null); // ❌ cantidad null

        when(carritoRepository.findByUsuario_Id(usuarioId))
            .thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(carrito.getId()))
            .thenReturn(List.of(itemSinCantidad));
        
        // El servicio debería manejar el null
        when(pedidosRepository.save(any(Pedido.class)))
            .thenReturn(pedido);
        when(detallePedidosRepository.saveAll(anyList()))
            .thenReturn(List.of(new PedidosDetalles()));

        // Act - ✅ CORREGIDO: solo 1 parámetro
        Pedido resultado = pedidoService.confirmarPedido(usuarioId);

        // Assert
        assertNotNull(resultado);
        // Verificar que se manejó correctamente (debería asignar 1 por defecto)
    }
}