package com.grupo3.tienda_ropa.cupon.dto;

import com.grupo3.tienda_ropa.cupon.entity.TipoDescuento;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CuponResponseDto {
    private Long id;
    private String codigo;
    private TipoDescuento tipoDescuento;
    private BigDecimal valor;
    private LocalDateTime fechaExpiracion;
    private BigDecimal montoMinimo;
    private Boolean activo;
    private Integer limiteUso;
    private Integer vecesUsado;
    private Boolean permiteMultiplesUsosPorCliente;
}
