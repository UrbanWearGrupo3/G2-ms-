package com.grupo3.tienda_ropa.producto.controllers;

import com.grupo3.tienda_ropa.producto.deto.CategoriaRequest;
import com.grupo3.tienda_ropa.producto.deto.CategoriaResponse;
import com.grupo3.tienda_ropa.producto.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse response = categoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarTodas(
            @RequestParam(required = false, defaultValue = "false") Boolean soloActivas) {
        List<CategoriaResponse> categorias = soloActivas
                ? categoriaService.listarActivas()
                : categoriaService.listarTodas();
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<CategoriaResponse> toggleActivo(@PathVariable Long id,
                                                           @RequestParam Boolean activo) {
        return ResponseEntity.ok(categoriaService.toggleActivo(id, activo));
    }
}
