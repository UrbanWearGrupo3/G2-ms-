package com.grupo3.tienda_ropa.Pedidos.deto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetallePedidosRequest {

    @NotNull
    private Long productoId;

    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}