package com.grupo3.tienda_ropa.carrito.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponse {

    private Long id;
    private Long usuarioId;

    private Integer totalItems;

    private Integer cantidadProductos;

    private BigDecimal totalPrecio;

    private List<CarritoItemResponse> items;
}