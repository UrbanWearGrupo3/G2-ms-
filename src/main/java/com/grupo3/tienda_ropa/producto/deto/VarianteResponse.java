package com.grupo3.tienda_ropa.producto.deto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VarianteResponse {
    private Long id;
    private String talle;
    private String color;
    private Integer stock;
    private String codigoBarras;
}
