package com.grupo3.tienda_ropa.producto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo3.tienda_ropa.producto.entity.ProductEntity;

public interface ProductoRepository extends JpaRepository<ProductEntity, Long> {
}