package com.grupo3.tienda_ropa.Pedidos.deto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DetallePedidosResponse {

    private Long productoVarianteId;

    private Integer cantidad;

    private BigDecimal precioUnitario;

    private BigDecimal subtotal;
}