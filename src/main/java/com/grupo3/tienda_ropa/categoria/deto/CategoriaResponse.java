package com.grupo3.tienda_ropa.categoria.deto;

import lombok.Data;

@Data
public class CategoriaResponse {
    private long id;
    private String nombre;
    private Number precio;
    private String talle;
    private String color;
    private Number stock;
}
