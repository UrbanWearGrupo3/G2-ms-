package com.grupo3.tienda_ropa.Resenias.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo3.tienda_ropa.Resenias.Entity.ReseniasEntity;

@Repository
public interface ReseniaRepository
        extends JpaRepository<ReseniasEntity, Long> {

    List<ReseniasEntity> findByProductoId(Long productoId);

    Optional<ReseniasEntity> findByUsuarioIdAndProductoId(
            Long usuarioId,
            Long productoId
    );
}