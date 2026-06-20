package com.grupo3.tienda_ropa.envio.repository;

import com.grupo3.tienda_ropa.envio.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByPedidoId(Long pedidoId);
    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);
}
