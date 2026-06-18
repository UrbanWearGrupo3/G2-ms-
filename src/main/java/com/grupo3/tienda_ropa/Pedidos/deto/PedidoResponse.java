package com.grupo3.tienda_ropa.Pedidos.deto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PedidoResponse {

 private Long id;

    private LocalDateTime fecha;

    private String estado;

    private BigDecimal total;

    private String direccionEnvio;

    private Long usuarioId;

    private List<DetallePedidosResponse> detalles;
}