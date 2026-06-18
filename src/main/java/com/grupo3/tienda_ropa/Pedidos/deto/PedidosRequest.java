package com.grupo3.tienda_ropa.Pedidos.deto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidosRequest {

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;

    @NotEmpty(message = "El pedido debe contener productos")
    private List<DetallePedidosRequest> detalles;
}