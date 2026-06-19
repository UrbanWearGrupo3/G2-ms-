package com.grupo3.tienda_ropa.carrito.repository;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository
        extends JpaRepository<CarritoEntity, Long> {

    Optional<CarritoEntity> findByUsuario_Id(Long usuarioId);

    boolean existsByUsuario_Id(Long usuarioId);
}