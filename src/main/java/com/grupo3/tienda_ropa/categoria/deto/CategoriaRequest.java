package com.grupo3.tienda_ropa.categoria.deto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoriaRequest {

    private String nombre;
    private Number precio;
    private String talle;
    private String color;
    private String imagen;
    private Number stock;
}

