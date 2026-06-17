package com.grupo3.tienda_ropa.producto.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.producto.entity.ProductEntity;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public ProductEntity guardar(ProductEntity producto) {
        return repository.save(producto);
    }

    public List<ProductEntity> listar() {
        return repository.findAll();
    }

}