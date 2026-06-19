package com.grupo3.tienda_ropa.producto.repository;

import com.grupo3.tienda_ropa.producto.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN p.variantes v " +
           "WHERE (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
           "AND (:talle IS NULL OR LOWER(v.talle) = LOWER(CAST(:talle AS string))) " +
           "AND (:color IS NULL OR LOWER(v.color) = LOWER(CAST(:color AS string))) " +
           "AND (:precioMin IS NULL OR p.precio >= :precioMin) " +
           "AND (:precioMax IS NULL OR p.precio <= :precioMax) " +
           "AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) " +
           "AND (p.activo = :activo)")
    Page<Producto> findByFiltros(
            @Param("categoriaId") Long categoriaId,
            @Param("talle") String talle,
            @Param("color") String color,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo,
            Pageable pageable);
}
