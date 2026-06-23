package com.grupo3.tienda_ropa.color.controller;

import com.grupo3.tienda_ropa.color.dto.ColorRequestDto;
import com.grupo3.tienda_ropa.color.dto.ColorResponseDto;
import com.grupo3.tienda_ropa.color.service.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colores")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @PostMapping
    public ResponseEntity<ColorResponseDto> crear(@Valid @RequestBody ColorRequestDto request) {
        ColorResponseDto response = colorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColorResponseDto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(colorService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<ColorResponseDto>> listarTodos(
            @RequestParam(required = false, defaultValue = "false") Boolean soloActivos) {
        List<ColorResponseDto> colores = soloActivos
                ? colorService.listarActivos()
                : colorService.listarTodos();
        return ResponseEntity.ok(colores);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColorResponseDto> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ColorRequestDto request) {
        return ResponseEntity.ok(colorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        colorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<ColorResponseDto> toggleActivo(@PathVariable Long id,
                                                          @RequestParam Boolean activo) {
        return ResponseEntity.ok(colorService.toggleActivo(id, activo));
    }
}
