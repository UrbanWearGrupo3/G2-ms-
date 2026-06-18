package com.grupo3.tienda_ropa.producto.deto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
