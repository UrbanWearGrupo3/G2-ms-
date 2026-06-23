package com.grupo3.tienda_ropa.cupon.dto;

import com.grupo3.tienda_ropa.cupon.entity.TipoDescuento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CuponRequestDto {

    @NotBlank(message = "El código de cupón no puede estar vacío")
    private String codigo;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor del descuento debe ser mayor a 0")
    private BigDecimal valor;

    @NotNull(message = "La fecha de expiración es obligatoria")
    @Future(message = "La fecha de expiración debe ser en el futuro")
    private LocalDateTime fechaExpiracion;

    @DecimalMin(value = "0.00", message = "El monto mínimo no puede ser negativo")
    private BigDecimal montoMinimo;

    private Boolean activo = true;

    private Integer limiteUso;

    private Boolean permiteMultiplesUsosPorCliente = false;
}
