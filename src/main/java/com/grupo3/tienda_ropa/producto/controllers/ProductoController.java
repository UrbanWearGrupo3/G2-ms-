package com.grupo3.tienda_ropa.producto.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.service.ProductoService;

@RestController
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public Producto save(@RequestBody Producto entity) {
        return productoService.save(entity);
    }

    @GetMapping("/{id}")
    public Optional<Producto> findById(Long id) {
        return productoService.findById(id);
    }

    @GetMapping
    public List<Producto> findAll() {
        return productoService.findAll();
    }

    @GetMapping("/{nombre}")
    public Optional<Producto> findByNombre(@PathVariable String nombre) {
        return productoService.findByNombre(nombre);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        productoService.deleteById(id);
    }

    @GetMapping("/buscar/{nombre}")
    public List<Producto> findByNombreContaining(@PathVariable String nombre) {
        return productoService.findByNombreContaining(nombre);
    }
}
