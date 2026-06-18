package com.grupo3.tienda_ropa.producto.controllers;

import com.grupo3.tienda_ropa.producto.deto.ProductoRequest;
import com.grupo3.tienda_ropa.producto.deto.ProductoResponse;
import com.grupo3.tienda_ropa.producto.deto.VarianteRequest;
import com.grupo3.tienda_ropa.producto.deto.VarianteResponse;
import com.grupo3.tienda_ropa.producto.service.ProductoService;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ProductoRequest productoRequest;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        productoRequest = new ProductoRequest();
        productoRequest.setNombre("Remera Slim Fit");
        productoRequest.setPrecio(new BigDecimal("15000.00"));
        productoRequest.setCategoriaId(1L);

        productoResponse = new ProductoResponse();
        productoResponse.setId(1L);
        productoResponse.setNombre("Remera Slim Fit");
        productoResponse.setPrecio(new BigDecimal("15000.00"));
        productoResponse.setActivo(true);
    }

    @Test
    void testCrearProducto() {
        when(productoService.save(any(ProductoRequest.class))).thenReturn(productoResponse);

        ResponseEntity<ProductoResponse> response = productoController.save(productoRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Remera Slim Fit", response.getBody().getNombre());
        verify(productoService, times(1)).save(any(ProductoRequest.class));
    }

    @Test
    void testObtenerProductoPorId() {
        when(productoService.findById(1L)).thenReturn(productoResponse);

        ResponseEntity<ProductoResponse> response = productoController.findById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(productoService, times(1)).findById(1L);
    }

    @Test
    void testListarProductosConFiltros() {
        Page<ProductoResponse> page = new PageImpl<>(Collections.singletonList(productoResponse));
        when(productoService.findAll(eq(1L), eq("M"), eq("Negro"), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<Page<ProductoResponse>> response = productoController.findAll(
                1L, "M", "Negro", null, null, null, null, PageRequest.of(0, 10)
        );

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Remera Slim Fit", response.getBody().getContent().get(0).getNombre());
        verify(productoService, times(1)).findAll(eq(1L), eq("M"), eq("Negro"), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    void testAgregarVariante() {
        VarianteRequest varRequest = new VarianteRequest();
        varRequest.setTalle("L");
        varRequest.setColor("Azul");
        varRequest.setStock(15);

        when(productoService.addVariante(eq(1L), any(VarianteRequest.class))).thenReturn(productoResponse);

        ResponseEntity<ProductoResponse> response = productoController.addVariante(1L, varRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productoService, times(1)).addVariante(eq(1L), any(VarianteRequest.class));
    }
}
