package com.grupo3.tienda_ropa.envio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotizacionResultDto {
    private String proveedor;
    private BigDecimal costo;
    private int diasEstimadosEntrega;
    private String descripcionServicio;
}
