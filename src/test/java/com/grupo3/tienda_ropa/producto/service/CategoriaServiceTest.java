package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.producto.deto.CategoriaRequest;
import com.grupo3.tienda_ropa.producto.deto.CategoriaResponse;
import com.grupo3.tienda_ropa.producto.entity.Categoria;
import com.grupo3.tienda_ropa.producto.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaRequest request;
    private Categoria categoriaEntity;

    @BeforeEach
    void setUp() {
        request = new CategoriaRequest();
        request.setNombre("Remeras");
        request.setDescripcion("Remeras de algodón");

        categoriaEntity = new Categoria();
        categoriaEntity.setId(1L);
        categoriaEntity.setNombre("Remeras");
        categoriaEntity.setDescripcion("Remeras de algodón");
        categoriaEntity.setActivo(true);
    }

    @Test
    void testCrear_Success() {
        when(categoriaRepository.existsByNombreIgnoreCase("Remeras")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaEntity);

        CategoriaResponse response = categoriaService.crear(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Remeras", response.getNombre());
        assertEquals("Remeras de algodón", response.getDescripcion());
        assertTrue(response.getActivo());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void testCrear_NombreDuplicado_ThrowsException() {
        when(categoriaRepository.existsByNombreIgnoreCase("Remeras")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> categoriaService.crear(request));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void testObtenerPorId_Success() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));

        CategoriaResponse response = categoriaService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals("Remeras", response.getNombre());
    }

    @Test
    void testObtenerPorId_NotFound() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> categoriaService.obtenerPorId(99L));
    }

    @Test
    void testListarTodas() {
        Categoria pantalones = new Categoria();
        pantalones.setId(2L);
        pantalones.setNombre("Pantalones");
        pantalones.setDescripcion("Pantalones de gabardina");
        pantalones.setActivo(true);

        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaEntity, pantalones));

        List<CategoriaResponse> result = categoriaService.listarTodas();

        assertEquals(2, result.size());
    }

    @Test
    void testActualizar_Success() {
        CategoriaRequest updateRequest = new CategoriaRequest();
        updateRequest.setNombre("Remeras Deportivas");
        updateRequest.setDescripcion("Remeras de poliéster");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(categoriaRepository.findByNombreIgnoreCase("Remeras Deportivas")).thenReturn(Optional.empty());
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse response = categoriaService.actualizar(1L, updateRequest);

        assertEquals("Remeras Deportivas", response.getNombre());
        assertEquals("Remeras de poliéster", response.getDescripcion());
    }

    @Test
    void testEliminar_SoftDelete() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        categoriaService.eliminar(1L);

        assertFalse(categoriaEntity.getActivo());
        verify(categoriaRepository).save(categoriaEntity);
    }

    @Test
    void testToggleActivo() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoriaResponse response = categoriaService.toggleActivo(1L, false);

        assertFalse(response.getActivo());
    }
}
