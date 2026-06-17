package com.grupo3.tienda_ropa.producto.deto;

import lombok.Data;

@Data
public class ProductResponse {
    private long id;
    private String nombre;
    private Number precio;
    private String talle;
    private String color;
    private Number stock;
}
