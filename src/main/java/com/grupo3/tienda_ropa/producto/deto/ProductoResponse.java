package com.grupo3.tienda_ropa.producto.deto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProductoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String marca;
    private String imagenUrl;
    private Boolean activo;
    private CategoriaResponse categoria;
    private List<VarianteResponse> variantes;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
