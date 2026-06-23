package com.grupo3.tienda_ropa.cupon.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class CuponDescuentoDto {
    private String codigo;
    private Boolean valido;
    private BigDecimal descuentoAplicado;
    private BigDecimal nuevoTotal;
    private String mensajeError;
}
