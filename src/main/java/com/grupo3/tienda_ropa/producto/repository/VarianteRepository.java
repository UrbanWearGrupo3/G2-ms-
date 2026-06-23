package com.grupo3.tienda_ropa.producto.repository;

import com.grupo3.tienda_ropa.producto.entity.Variante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteRepository extends JpaRepository<Variante, Long> {
    Optional<Variante> findByCodigoBarras(String codigoBarras);
    List<Variante> findByProductoId(Long productoId);
    boolean existsByProductoIdAndTalleIgnoreCaseAndColorId(Long productoId, String talle, Long colorId);
    Optional<Variante> findById(Long varianteId);
}
