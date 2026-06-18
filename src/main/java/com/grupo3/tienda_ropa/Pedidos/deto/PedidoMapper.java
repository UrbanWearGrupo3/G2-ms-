package com.grupo3.tienda_ropa.Pedidos.deto;

import com.grupo3.tienda_ropa.Pedidos.entity.DetallePedidoEntity;
import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import org.springframework.stereotype.Component;

@Component
public class PedidoMapper {

    public PedidoResponse toResponse(Pedido pedido) {

        return PedidoResponse.builder()
                .id(pedido.getId())
                .fecha(pedido.getFecha())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .direccionEnvio(pedido.getDireccionEnvio())
                .usuarioId(pedido.getUsuario().getId())
                .detalles(
                        pedido.getDetalles()
                                .stream()
                                .map(this::toDetalleResponse)
                                .toList()
                )
                .build();
    }

    private DetallePedidosResponse toDetalleResponse(
            DetallePedidoEntity detalle
    ) {

        return DetallePedidosResponse.builder()
                .productoVarianteId(detalle.getProductoVarianteId())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .build();
    }
}