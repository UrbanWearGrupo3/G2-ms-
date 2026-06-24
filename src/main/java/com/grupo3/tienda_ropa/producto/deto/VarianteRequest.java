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

    @NotNull(message = "El color es obligatorio")
    private Long colorId;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private String codigoBarras;

    private Long id;

    private Boolean activo = true;
}
