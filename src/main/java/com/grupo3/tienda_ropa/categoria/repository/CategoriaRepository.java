package com.grupo3.tienda_ropa.categoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grupo3.tienda_ropa.categoria.entity.CatergoriaEntity;

public interface CategoriaRepository extends JpaRepository<CatergoriaEntity, Long> {
}