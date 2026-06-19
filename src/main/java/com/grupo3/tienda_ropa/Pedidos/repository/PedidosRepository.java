package com.grupo3.tienda_ropa.Pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;

import java.util.List;

@Repository
public interface PedidosRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioId(Long usuarioId);

    List<Pedido> findByEstado(String estado);

    List<Pedido> findByUsuarioIdAndEstado(Long usuarioId, String estado);

}