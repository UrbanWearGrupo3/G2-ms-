package com.grupo3.tienda_ropa.producto.deto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductoRequest {

    private String nombre;
    private Number precio;
    private String talle;
    private String color;
    private String imagen;
    private Number stock;
}

