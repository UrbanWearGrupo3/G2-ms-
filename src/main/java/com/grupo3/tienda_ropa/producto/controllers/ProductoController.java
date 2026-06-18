package com.grupo3.tienda_ropa.producto.controllers;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grupo3.tienda_ropa.producto.entity.ProductEntity;
import com.grupo3.tienda_ropa.producto.service.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @PostMapping
    public ProductEntity saveProduct(@RequestBody ProductEntity producto) {
        return service.saveProduct(producto);
    }

    @GetMapping
    public List<ProductEntity> findAllProducts() {
        return service.findAllProducts();
    }
    
     @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> buscarPorId(@PathVariable Long id) {
        return service.getForId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    //get for name
    @GetMapping("/nombre")
    public ResponseEntity<List<ProductEntity>> getForName(@RequestParam String nombre) {
    if (nombre == null || nombre.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
    }
    
    List<ProductEntity> productos = service.getForName(nombre);
    
    if (productos.isEmpty()) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
        return ResponseEntity.ok(productos);
    }
    //Get for category
    //getProductsByCategory
        @GetMapping("/categoria")
    public ResponseEntity<List<ProductEntity>> getForCategory(@RequestParam String categoria) {
    if (categoria == null || categoria.trim().isEmpty()) {
        return ResponseEntity.badRequest().build();
    }
    
    List<ProductEntity> productos = service.getProductsByCategory(categoria);
    
    if (productos.isEmpty()) {
        return ResponseEntity.ok(Collections.emptyList());
    }
    
        return ResponseEntity.ok(productos);
    }
    
}
