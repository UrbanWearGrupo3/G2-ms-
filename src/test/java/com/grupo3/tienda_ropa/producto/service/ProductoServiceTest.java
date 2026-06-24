package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.color.entity.Color;
import com.grupo3.tienda_ropa.color.repository.ColorRepository;
import com.grupo3.tienda_ropa.producto.deto.ProductoRequest;
import com.grupo3.tienda_ropa.producto.deto.ProductoResponse;
import com.grupo3.tienda_ropa.producto.deto.VarianteRequest;
import com.grupo3.tienda_ropa.producto.entity.Categoria;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.entity.Variante;
import com.grupo3.tienda_ropa.producto.repository.CategoriaRepository;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import com.grupo3.tienda_ropa.producto.repository.VarianteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private VarianteRepository varianteRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private SupabaseStorageService storageService;
    @Mock
    private ColorRepository colorRepository;

    @InjectMocks
    private ProductoService productoService;

    private Categoria categoria;
    private Color color;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Remeras");
        categoria.setDescripcion("Remeras de algodon");
        categoria.setActivo(true);

        color = new Color();
        color.setId(1L);
        color.setNombre("Rojo");
        color.setCodigoHex("#FF0000");
        color.setActivo(true);
    }

    @Test
    void testUpdate_DeactivatesRemovedVariant() {
        // Arrange: Existing product with 1 variant
        Producto existingProduct = new Producto();
        existingProduct.setId(1L);
        existingProduct.setNombre("Remera");
        existingProduct.setCategoria(categoria);
        existingProduct.setPrecio(new BigDecimal("1000.00"));
        existingProduct.setActivo(true);

        Variante v1 = new Variante();
        v1.setId(10L);
        v1.setTalle("M");
        v1.setColor(color);
        v1.setStock(5);
        v1.setActivo(true);
        v1.setCodigoBarras("123456789");
        v1.setProducto(existingProduct);
        
        List<Variante> list = new ArrayList<>();
        list.add(v1);
        existingProduct.setVariantes(list);

        // Request: Update product, but NO variants (meaning the user removed the variant)
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Remera");
        request.setPrecio(new BigDecimal("1000.00"));
        request.setCategoriaId(1L);
        request.setVariantes(new ArrayList<>()); // Empty list

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProductoResponse response = productoService.update(1L, request);

        // Assert
        assertNotNull(response);
        verify(productoRepository).save(existingProduct);
        assertEquals(1, existingProduct.getVariantes().size());
        
        Variante updatedVariant = existingProduct.getVariantes().get(0);
        assertEquals("M", updatedVariant.getTalle());
        assertFalse(updatedVariant.getActivo()); // Soft-deleted/deactivated!
    }

    @Test
    void testUpdate_PreservesAndUpdatesVariant() {
        // Arrange
        Producto existingProduct = new Producto();
        existingProduct.setId(1L);
        existingProduct.setNombre("Remera");
        existingProduct.setCategoria(categoria);
        existingProduct.setPrecio(new BigDecimal("1000.00"));
        existingProduct.setActivo(true);

        Variante v1 = new Variante();
        v1.setId(10L);
        v1.setTalle("M");
        v1.setColor(color);
        v1.setStock(5);
        v1.setActivo(true);
        v1.setCodigoBarras("123456789");
        v1.setProducto(existingProduct);
        
        List<Variante> list = new ArrayList<>();
        list.add(v1);
        existingProduct.setVariantes(list);

        // Request: Update variant stock to 10
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Remera");
        request.setPrecio(new BigDecimal("1000.00"));
        request.setCategoriaId(1L);
        
        VarianteRequest vr = new VarianteRequest();
        vr.setId(10L);
        vr.setTalle("M");
        vr.setColorId(1L);
        vr.setStock(10);
        vr.setActivo(true);
        vr.setCodigoBarras("123456789");
        
        request.setVariantes(List.of(vr));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ProductoResponse response = productoService.update(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(1, existingProduct.getVariantes().size());
        Variante updatedVariant = existingProduct.getVariantes().get(0);
        assertEquals(10, updatedVariant.getStock());
        assertTrue(updatedVariant.getActivo());
    }
}
