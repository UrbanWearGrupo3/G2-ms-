package com.grupo3.tienda_ropa.envio;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.envio.dto.CotizacionResultDto;
import com.grupo3.tienda_ropa.envio.dto.EnvioResultDto;
import com.grupo3.tienda_ropa.envio.event.ShipmentStatusChangedEvent;
import com.grupo3.tienda_ropa.envio.model.Envio;
import com.grupo3.tienda_ropa.envio.model.EnvioEstado;
import com.grupo3.tienda_ropa.envio.repository.EnvioRepository;
import com.grupo3.tienda_ropa.envio.service.EnvioService;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategy;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private PedidosRepository pedidosRepository;

    @Mock
    private EnvioStrategyContext strategyContext;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EnvioStrategy shippingStrategy;

    private EnvioService envioService;

    @BeforeEach
    void setUp() {
        envioService = new EnvioService(envioRepository, pedidosRepository, strategyContext, eventPublisher);
    }

    @Test
    void testCotizarEnvio_Success() {
        // Arrange
        Long pedidoId = 1L;
        String proveedor = "CORREO_ARGENTINO";
        String direccion = "Av. Siempre Viva 742";
        Pedido mockPedido = new Pedido();
        mockPedido.setId(pedidoId);

        CotizacionResultDto mockCotizacion = CotizacionResultDto.builder()
                .proveedor(proveedor)
                .costo(new BigDecimal("4500.00"))
                .diasEstimadosEntrega(5)
                .build();

        when(pedidosRepository.findById(pedidoId)).thenReturn(Optional.of(mockPedido));
        when(strategyContext.getStrategy(proveedor)).thenReturn(shippingStrategy);
        when(shippingStrategy.cotizarEnvio(mockPedido, direccion)).thenReturn(mockCotizacion);

        // Act
        CotizacionResultDto result = envioService.cotizarEnvio(pedidoId, proveedor, direccion);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("4500.00"), result.getCosto());
        assertEquals(proveedor, result.getProveedor());
        verify(pedidosRepository).findById(pedidoId);
        verify(shippingStrategy).cotizarEnvio(mockPedido, direccion);
    }

    @Test
    void testCrearEnvio_Success() {
        // Arrange
        Long pedidoId = 1L;
        String proveedor = "CORREO_ARGENTINO";
        String direccion = "Av. Siempre Viva 742";
        Pedido mockPedido = new Pedido();
        mockPedido.setId(pedidoId);

        EnvioResultDto mockResult = EnvioResultDto.builder()
                .proveedor(proveedor)
                .codigoSeguimiento("SD-12345-AR")
                .costo(new BigDecimal("4500.00"))
                .estadoInicial("PREPARANDO")
                .build();

        when(envioRepository.findByPedidoId(pedidoId)).thenReturn(Optional.empty());
        when(pedidosRepository.findById(pedidoId)).thenReturn(Optional.of(mockPedido));
        when(strategyContext.getStrategy(proveedor)).thenReturn(shippingStrategy);
        when(shippingStrategy.getProveedor()).thenReturn(proveedor);
        when(shippingStrategy.crearEnvio(mockPedido, direccion)).thenReturn(mockResult);
        
        // Simular comportamiento de guardado de repositorio
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Envio result = envioService.crearEnvio(pedidoId, proveedor, direccion);

        // Assert
        assertNotNull(result);
        assertEquals(proveedor, result.getProveedorEnvio());
        assertEquals("SD-12345-AR", result.getCodigoSeguimiento());
        assertEquals(new BigDecimal("4500.00"), result.getCosto());
        assertEquals(direccion, result.getDireccionDestino());
        assertEquals(EnvioEstado.PREPARANDO, result.getEstado());

        // Verificar cambio de estado en el pedido
        assertEquals("DESPACHADO", mockPedido.getEstado());
        assertEquals(direccion, mockPedido.getDireccionEnvio());
        verify(pedidosRepository).save(mockPedido);

        // Verificar que el evento se haya publicado
        ArgumentCaptor<ShipmentStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(result, eventCaptor.getValue().envio());
    }

    @Test
    void testActualizarEstadoEnvio_Success() {
        // Arrange
        Long envioId = 100L;
        Pedido mockPedido = new Pedido();
        mockPedido.setId(1L);
        mockPedido.setEstado("PENDIENTE");

        Envio mockEnvio = new Envio();
        mockEnvio.setId(envioId);
        mockEnvio.setEstado(EnvioEstado.PREPARANDO);
        mockEnvio.setPedido(mockPedido);

        when(envioRepository.findById(envioId)).thenReturn(Optional.of(mockEnvio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Envio result = envioService.actualizarEstadoEnvio(envioId, EnvioEstado.ENTREGADO);

        // Assert
        assertNotNull(result);
        assertEquals(EnvioEstado.ENTREGADO, result.getEstado());
        assertEquals("ENTREGADO", mockPedido.getEstado());
        verify(pedidosRepository).save(mockPedido);

        ArgumentCaptor<ShipmentStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ShipmentStatusChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(result, eventCaptor.getValue().envio());
    }

    @Test
    void testConsultarTrackingDesdeProveedor_Success() {
        // Arrange
        Long envioId = 100L;
        Envio mockEnvio = new Envio();
        mockEnvio.setId(envioId);
        mockEnvio.setCodigoSeguimiento("OCA-12345");
        mockEnvio.setProveedorEnvio("OCA");
        mockEnvio.setEstado(EnvioEstado.EN_TRANSITO);

        when(envioRepository.findById(envioId)).thenReturn(Optional.of(mockEnvio));
        when(strategyContext.getStrategy("OCA")).thenReturn(shippingStrategy);
        when(shippingStrategy.consultarSeguimiento("OCA-12345", EnvioEstado.EN_TRANSITO)).thenReturn("Estado OCA: Envío en tránsito local");

        // Act
        String trackingInfo = envioService.consultarTrackingDesdeProveedor(envioId);

        // Assert
        assertEquals("Estado OCA: Envío en tránsito local", trackingInfo);
        verify(envioRepository).findById(envioId);
        verify(shippingStrategy).consultarSeguimiento("OCA-12345", EnvioEstado.EN_TRANSITO);
    }
}
