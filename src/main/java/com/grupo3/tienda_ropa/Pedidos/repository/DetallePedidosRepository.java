package com.grupo3.tienda_ropa.Pedidos.repository;



import com.grupo3.tienda_ropa.Pedidos.entity.PedidosDetalles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidosRepository extends JpaRepository<PedidosDetalles, Long> {
    List<PedidosDetalles> findByPedidoId(Long pedidoId);

}