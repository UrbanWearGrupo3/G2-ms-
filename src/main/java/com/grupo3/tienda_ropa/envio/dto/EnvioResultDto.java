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
public class EnvioResultDto {
    private String proveedor;
    private String codigoSeguimiento;
    private BigDecimal costo;
    private String estadoInicial;
    private String etiquetaBase64; // Mock tag or pdf base64
}
