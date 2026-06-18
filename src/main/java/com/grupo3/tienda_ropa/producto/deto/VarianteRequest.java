package com.grupo3.tienda_ropa.producto.deto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VarianteRequest {

    @NotBlank(message = "El talle es obligatorio")
    private String talle;

    @NotBlank(message = "El color es obligatorio")
    private String color;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private String codigoBarras;
}
