package com.grupo3.tienda_ropa.carrito.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.grupo3.tienda_ropa.producto.deto.VarianteResponse;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponse {

    private Long id;
    private Integer cantidad;

    private Long productoId;
    private String nombreProducto;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precio;

    private List<VarianteResponse> variantes;

    private BigDecimal subtotal;
}