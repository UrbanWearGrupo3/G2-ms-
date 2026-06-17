package com.grupo3.tienda_ropa.producto.repository;

import com.grupo3.tienda_ropa.producto.entity.Producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Se definen metodos de busqueda
    <S extends Producto> S save(S entity);

    Optional<Producto> findById(Long id);

    List<Producto> findAll();

    void deleteById(Long id);

    Optional<Producto> findByNombre(String nombre);

    List<Producto> findByNombreContaining(String nombre);

}
