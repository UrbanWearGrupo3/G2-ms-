package com.grupo3.tienda_ropa.producto.deto;

import com.grupo3.tienda_ropa.color.dto.ColorResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VarianteResponse {
    private Long id;
    private String talle;
    private ColorResponseDto color;
    private Integer stock;
    private String codigoBarras;
    private Boolean activo = true;
}
