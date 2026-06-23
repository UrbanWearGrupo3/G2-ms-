package com.grupo3.tienda_ropa.color.service;

import com.grupo3.tienda_ropa.color.dto.ColorRequestDto;
import com.grupo3.tienda_ropa.color.dto.ColorResponseDto;
import com.grupo3.tienda_ropa.color.entity.Color;
import com.grupo3.tienda_ropa.color.repository.ColorRepository;
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
class ColorServiceTest {

    @Mock
    private ColorRepository colorRepository;

    @InjectMocks
    private ColorService colorService;

    private ColorRequestDto request;
    private Color colorEntity;

    @BeforeEach
    void setUp() {
        request = new ColorRequestDto();
        request.setNombre("Negro");
        request.setCodigoHex("#000000");

        colorEntity = new Color();
        colorEntity.setId(1L);
        colorEntity.setNombre("Negro");
        colorEntity.setCodigoHex("#000000");
        colorEntity.setActivo(true);
    }

    @Test
    void testCrear_Success() {
        when(colorRepository.existsByNombreIgnoreCase("Negro")).thenReturn(false);
        when(colorRepository.save(any(Color.class))).thenReturn(colorEntity);

        ColorResponseDto response = colorService.crear(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Negro", response.getNombre());
        assertEquals("#000000", response.getCodigoHex());
        assertTrue(response.getActivo());
        verify(colorRepository).save(any(Color.class));
    }

    @Test
    void testCrear_NombreDuplicado_ThrowsException() {
        when(colorRepository.existsByNombreIgnoreCase("Negro")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> colorService.crear(request));
        verify(colorRepository, never()).save(any());
    }

    @Test
    void testObtenerPorId_Success() {
        when(colorRepository.findById(1L)).thenReturn(Optional.of(colorEntity));

        ColorResponseDto response = colorService.obtenerPorId(1L);

        assertNotNull(response);
        assertEquals("Negro", response.getNombre());
    }

    @Test
    void testObtenerPorId_NotFound() {
        when(colorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> colorService.obtenerPorId(99L));
    }

    @Test
    void testListarTodos() {
        Color blanco = new Color();
        blanco.setId(2L);
        blanco.setNombre("Blanco");
        blanco.setCodigoHex("#FFFFFF");
        blanco.setActivo(true);

        when(colorRepository.findAll()).thenReturn(List.of(colorEntity, blanco));

        List<ColorResponseDto> result = colorService.listarTodos();

        assertEquals(2, result.size());
    }

    @Test
    void testActualizar_Success() {
        ColorRequestDto updateRequest = new ColorRequestDto();
        updateRequest.setNombre("Negro Mate");
        updateRequest.setCodigoHex("#1A1A1A");

        when(colorRepository.findById(1L)).thenReturn(Optional.of(colorEntity));
        when(colorRepository.findByNombreIgnoreCase("Negro Mate")).thenReturn(Optional.empty());
        when(colorRepository.save(any(Color.class))).thenAnswer(inv -> inv.getArgument(0));

        ColorResponseDto response = colorService.actualizar(1L, updateRequest);

        assertEquals("Negro Mate", response.getNombre());
        assertEquals("#1A1A1A", response.getCodigoHex());
    }

    @Test
    void testEliminar_SoftDelete() {
        when(colorRepository.findById(1L)).thenReturn(Optional.of(colorEntity));
        when(colorRepository.save(any(Color.class))).thenAnswer(inv -> inv.getArgument(0));

        colorService.eliminar(1L);

        assertFalse(colorEntity.getActivo());
        verify(colorRepository).save(colorEntity);
    }

    @Test
    void testToggleActivo() {
        when(colorRepository.findById(1L)).thenReturn(Optional.of(colorEntity));
        when(colorRepository.save(any(Color.class))).thenAnswer(inv -> inv.getArgument(0));

        ColorResponseDto response = colorService.toggleActivo(1L, false);

        assertFalse(response.getActivo());
    }
}
