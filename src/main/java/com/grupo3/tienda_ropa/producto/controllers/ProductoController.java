package com.grupo3.tienda_ropa.producto.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.producto.entity.ProductEntity;
import com.grupo3.tienda_ropa.producto.service.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    //subir productos
    @PostMapping
    public ProductEntity guardar(@RequestBody ProductEntity producto) {
        return service.guardar(producto);
    }

    //mostrar todos los productos
    @GetMapping
    public List<ProductEntity> listar() {
        return service.listar();
    }
}
