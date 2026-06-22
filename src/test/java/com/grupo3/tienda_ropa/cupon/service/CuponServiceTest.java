package com.grupo3.tienda_ropa.cupon.service;

import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.cupon.dto.CuponDescuentoDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponRequestDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponResponseDto;
import com.grupo3.tienda_ropa.cupon.entity.Cupon;
import com.grupo3.tienda_ropa.cupon.entity.TipoDescuento;
import com.grupo3.tienda_ropa.cupon.repository.CuponRepository;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock
    private CuponRepository cuponRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private CarritoItemRepo carritoItemRepo;

    @Mock
    private PedidosRepository pedidosRepository;

    private CuponService cuponService;

    @BeforeEach
    void setUp() {
        cuponService = new CuponService(
                cuponRepository,
                carritoRepository,
                carritoItemRepo,
                pedidosRepository
        );
    }

    @Test
    void testCrearCupon_Success() {
        CuponRequestDto dto = new CuponRequestDto();
        dto.setCodigo("promo10");
        dto.setTipoDescuento(TipoDescuento.PORCENTAJE);
        dto.setValor(BigDecimal.valueOf(10));
        dto.setFechaExpiracion(LocalDateTime.now().plusDays(2));
        dto.setActivo(true);

        when(cuponRepository.existsByCodigoIgnoreCase("promo10")).thenReturn(false);
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        CuponResponseDto result = cuponService.crearCupon(dto);

        assertNotNull(result);
        assertEquals("PROMO10", result.getCodigo()); // Should capitalize
        assertEquals(TipoDescuento.PORCENTAJE, result.getTipoDescuento());
        assertEquals(BigDecimal.valueOf(10), result.getValor());
        verify(cuponRepository, times(1)).save(any(Cupon.class));
    }

    @Test
    void testCrearCupon_DuplicateCode_ThrowsException() {
        CuponRequestDto dto = new CuponRequestDto();
        dto.setCodigo("DUPLICADO");

        when(cuponRepository.existsByCodigoIgnoreCase("DUPLICADO")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> cuponService.crearCupon(dto));
        verify(cuponRepository, never()).save(any(Cupon.class));
    }

    @Test
    void testValidarYCalcularDescuento_CouponNotFound() {
        when(cuponRepository.findByCodigoIgnoreCase("INVENTADO")).thenReturn(Optional.empty());

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("INVENTADO", 1L);

        assertFalse(result.getValido());
        assertEquals("El cupón no existe.", result.getMensajeError());
    }

    @Test
    void testValidarYCalcularDescuento_CouponInactive() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("OFF50");
        cupon.setActivo(false);

        when(cuponRepository.findByCodigoIgnoreCase("OFF50")).thenReturn(Optional.of(cupon));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("OFF50", 1L);

        assertFalse(result.getValido());
        assertEquals("El cupón no está activo.", result.getMensajeError());
    }

    @Test
    void testValidarYCalcularDescuento_CouponExpired() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("OFF50");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));

        when(cuponRepository.findByCodigoIgnoreCase("OFF50")).thenReturn(Optional.of(cupon));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("OFF50", 1L);

        assertFalse(result.getValido());
        assertEquals("El cupón ha expirado.", result.getMensajeError());
    }

    @Test
    void testValidarYCalcularDescuento_UsageLimitReached() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("OFF50");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        cupon.setLimiteUso(5);
        cupon.setVecesUsado(5);

        when(cuponRepository.findByCodigoIgnoreCase("OFF50")).thenReturn(Optional.of(cupon));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("OFF50", 1L);

        assertFalse(result.getValido());
        assertEquals("El cupón ha alcanzado su límite de uso global.", result.getMensajeError());
    }

    @Test
    void testValidarYCalcularDescuento_AlreadyUsedByClient() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("OFF50");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        cupon.setPermiteMultiplesUsosPorCliente(false);

        when(cuponRepository.findByCodigoIgnoreCase("OFF50")).thenReturn(Optional.of(cupon));
        when(pedidosRepository.existsByUsuarioIdAndCuponCodigoIgnoreCaseAndEstadoNotIn(
                eq(1L), eq("OFF50"), anyCollection()
        )).thenReturn(true);

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("OFF50", 1L);

        assertFalse(result.getValido());
        assertEquals("Ya has utilizado este cupón en una compra anterior.", result.getMensajeError());
    }

    @Test
    void testValidarYCalcularDescuento_MontoMinimoNotMet() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("BIGBUY");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        cupon.setPermiteMultiplesUsosPorCliente(true);
        cupon.setMontoMinimo(BigDecimal.valueOf(5000));

        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(10L);

        Producto producto = new Producto();
        producto.setPrecio(BigDecimal.valueOf(1000));

        CarritoItem item = new CarritoItem();
        item.setProducto(producto);
        item.setCantidad(3); // Total 3000 < 5000

        when(cuponRepository.findByCodigoIgnoreCase("BIGBUY")).thenReturn(Optional.of(cupon));
        when(carritoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(10L)).thenReturn(List.of(item));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("BIGBUY", 1L);

        assertFalse(result.getValido());
        assertEquals(BigDecimal.valueOf(3000), result.getNuevoTotal());
        assertTrue(result.getMensajeError().contains("El monto de la compra no alcanza el mínimo"));
    }

    @Test
    void testValidarYCalcularDescuento_Success_Percentage() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("PROMO10");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        cupon.setPermiteMultiplesUsosPorCliente(true);
        cupon.setTipoDescuento(TipoDescuento.PORCENTAJE);
        cupon.setValor(BigDecimal.valueOf(15)); // 15% discount

        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(10L);

        Producto producto = new Producto();
        producto.setPrecio(BigDecimal.valueOf(2000));

        CarritoItem item = new CarritoItem();
        item.setProducto(producto);
        item.setCantidad(2); // Total 4000

        when(cuponRepository.findByCodigoIgnoreCase("PROMO10")).thenReturn(Optional.of(cupon));
        when(carritoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(10L)).thenReturn(List.of(item));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("PROMO10", 1L);

        assertTrue(result.getValido());
        assertNull(result.getMensajeError());
        // 4000 * 0.15 = 600.00 discount
        assertEquals(0, BigDecimal.valueOf(600).compareTo(result.getDescuentoAplicado()));
        assertEquals(0, BigDecimal.valueOf(3400).compareTo(result.getNuevoTotal()));
    }

    @Test
    void testValidarYCalcularDescuento_Success_Fixed() {
        Cupon cupon = new Cupon();
        cupon.setCodigo("CASH500");
        cupon.setActivo(true);
        cupon.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        cupon.setPermiteMultiplesUsosPorCliente(true);
        cupon.setTipoDescuento(TipoDescuento.FIJO);
        cupon.setValor(BigDecimal.valueOf(500)); // $500 discount

        CarritoEntity carrito = new CarritoEntity();
        carrito.setId(10L);

        Producto producto = new Producto();
        producto.setPrecio(BigDecimal.valueOf(1500));

        CarritoItem item = new CarritoItem();
        item.setProducto(producto);
        item.setCantidad(1); // Total 1500

        when(cuponRepository.findByCodigoIgnoreCase("CASH500")).thenReturn(Optional.of(cupon));
        when(carritoRepository.findByUsuario_Id(1L)).thenReturn(Optional.of(carrito));
        when(carritoItemRepo.findByCarritoId(10L)).thenReturn(List.of(item));

        CuponDescuentoDto result = cuponService.validarYCalcularDescuento("CASH500", 1L);

        assertTrue(result.getValido());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(result.getDescuentoAplicado()));
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(result.getNuevoTotal()));
    }
}
