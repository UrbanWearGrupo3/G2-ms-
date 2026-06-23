package com.grupo3.tienda_ropa.color.repository;

import com.grupo3.tienda_ropa.color.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    Optional<Color> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Color> findByActivoTrue();
}
