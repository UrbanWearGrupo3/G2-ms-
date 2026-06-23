package com.grupo3.tienda_ropa.color.service;

import com.grupo3.tienda_ropa.color.dto.ColorRequestDto;
import com.grupo3.tienda_ropa.color.dto.ColorResponseDto;
import com.grupo3.tienda_ropa.color.entity.Color;
import com.grupo3.tienda_ropa.color.repository.ColorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    @Transactional
    public ColorResponseDto crear(ColorRequestDto request) {
        if (colorRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe un color con el nombre: " + request.getNombre());
        }

        Color color = new Color();
        color.setNombre(request.getNombre());
        color.setCodigoHex(request.getCodigoHex());
        color.setActivo(true);

        Color saved = colorRepository.save(color);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ColorResponseDto obtenerPorId(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Color no encontrado con id: " + id));
        return mapToResponse(color);
    }

    @Transactional(readOnly = true)
    public List<ColorResponseDto> listarTodos() {
        return colorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ColorResponseDto> listarActivos() {
        return colorRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ColorResponseDto actualizar(Long id, ColorRequestDto request) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Color no encontrado con id: " + id));

        // Validar que no exista otro color con el mismo nombre
        colorRepository.findByNombreIgnoreCase(request.getNombre())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe otro color con el nombre: " + request.getNombre());
                    }
                });

        color.setNombre(request.getNombre());
        color.setCodigoHex(request.getCodigoHex());

        Color saved = colorRepository.save(color);
        return mapToResponse(saved);
    }

    @Transactional
    public void eliminar(Long id) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Color no encontrado con id: " + id));
        color.setActivo(false);
        colorRepository.save(color);
    }

    @Transactional
    public ColorResponseDto toggleActivo(Long id, Boolean activo) {
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Color no encontrado con id: " + id));
        color.setActivo(activo);
        Color saved = colorRepository.save(color);
        return mapToResponse(saved);
    }

    private ColorResponseDto mapToResponse(Color color) {
        return ColorResponseDto.builder()
                .id(color.getId())
                .nombre(color.getNombre())
                .codigoHex(color.getCodigoHex())
                .activo(color.getActivo())
                .build();
    }
}
