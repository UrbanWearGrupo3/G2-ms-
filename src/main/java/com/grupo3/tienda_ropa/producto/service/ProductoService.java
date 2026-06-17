package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public <S extends Producto> S save(S entity) {
        return productoRepository.save(entity);
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    public Optional<Producto> findByNombre(String nombre) {
        return productoRepository.findByNombre(nombre);
    }

    public List<Producto> findByNombreContaining(String nombre) {
        return productoRepository.findByNombreContaining(nombre);
    }
}
