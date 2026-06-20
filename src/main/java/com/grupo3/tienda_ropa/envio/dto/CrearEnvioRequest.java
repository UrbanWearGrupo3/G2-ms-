package com.grupo3.tienda_ropa.envio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearEnvioRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotBlank(message = "El proveedor de envío es obligatorio")
    private String proveedor;

    @NotBlank(message = "La dirección de destino es obligatoria")
    private String direccionDestino;
}
