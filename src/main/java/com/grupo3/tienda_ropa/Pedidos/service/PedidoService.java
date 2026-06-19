package com.grupo3.tienda_ropa.Pedidos.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.entity.PedidosDetalles;
import com.grupo3.tienda_ropa.Pedidos.repository.DetallePedidosRepository;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepo carritoItemRepo;
    private final PedidosRepository pedidosRepository;
    private final DetallePedidosRepository detallePedidosRepository;

    public Pedido confirmarPedido(Long usuarioId) {

        CarritoEntity carrito = carritoRepository
                .findByUsuario_Id(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Carrito no encontrado"));

        List<CarritoItem> items =
                carritoItemRepo.findByCarritoId(carrito.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

       // ...existing code...
        Pedido pedido = new Pedido();

        pedido.setUsuario(carrito.getUsuario());
        // Si tu entidad Pedido tiene estado
        pedido.setEstado("PENDIENTE");

        Pedido pedidoGuardado = pedidosRepository.save(pedido);

        List<PedidosDetalles> detalles = items.stream()
                .map(item -> {

                    PedidosDetalles detalle = new PedidosDetalles();

                    detalle.setPedido(pedidoGuardado);
                    detalle.setProducto(item.getProducto());
                    detalle.setCantidad(item.getCantidad());

                    return detalle;
                })
                .toList();

        detallePedidosRepository.saveAll(detalles);

        carritoItemRepo.deleteAll(items);

       return pedidoGuardado;
    }


    public List<Pedido> obtenerPedidosUsuario(Long usuarioId) {
        return pedidosRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidosRepository.findByEstado(estado);
    }

    public List<Pedido> obtenerPedidosUsuarioPorEstado(
            Long usuarioId,
            String estado
    ) {
        return pedidosRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }
    public List<Pedido> obtenerTodosLosPedidos() {
    return pedidosRepository.findAll();
    }
    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {

        Pedido pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);

        return pedidosRepository.save(pedido);
    }
}