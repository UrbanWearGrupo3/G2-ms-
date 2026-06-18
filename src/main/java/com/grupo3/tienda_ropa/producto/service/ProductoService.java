package com.grupo3.tienda_ropa.producto.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo3.tienda_ropa.producto.entity.ProductEntity;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;

@Service
@Transactional

public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public ProductEntity saveProduct(ProductEntity producto) {
        return repository.save(producto);
    }

    public List<ProductEntity> findAllProducts() {
        return repository.findAll();
    }

    public Optional<ProductEntity> getForId(Long id) {
        return repository.findById(id);
    }

    public List<ProductEntity> getForName(String nombre) {
        return repository.findByNombreContainingIgnoreCase(nombre);
    }
  public List<ProductEntity> getProductsByCategory(String categoria) {
        return repository.findByCategoria_Categoria(categoria);
    }
}