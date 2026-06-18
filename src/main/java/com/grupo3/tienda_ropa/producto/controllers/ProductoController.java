package com.grupo3.tienda_ropa.producto.controllers;

import com.grupo3.tienda_ropa.producto.deto.ProductoRequest;
import com.grupo3.tienda_ropa.producto.deto.ProductoResponse;
import com.grupo3.tienda_ropa.producto.deto.VarianteRequest;
import com.grupo3.tienda_ropa.producto.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> save(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse saved = productoService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> findById(@PathVariable Long id) {
        ProductoResponse response = productoService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> findAll(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String talle,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        Page<ProductoResponse> page = productoService.findAll(categoriaId, talle, color, 
                precioMin, precioMax, nombre, activo, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> update(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductoRequest request) {
        ProductoResponse updated = productoService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        productoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activo")
    public ResponseEntity<ProductoResponse> toggleActivo(@PathVariable Long id, 
                                                         @RequestParam Boolean activo) {
        ProductoResponse response = productoService.toggleActivo(id, activo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/variantes")
    public ResponseEntity<ProductoResponse> addVariante(@PathVariable Long id, 
                                                        @Valid @RequestBody VarianteRequest request) {
        ProductoResponse response = productoService.addVariante(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/variantes/{varianteId}/stock")
    public ResponseEntity<ProductoResponse> updateStock(@PathVariable Long varianteId, 
                                                        @RequestParam Integer stock) {
        ProductoResponse response = productoService.updateStock(varianteId, stock);
        return ResponseEntity.ok(response);
    }
}
