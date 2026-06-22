package com.grupo3.tienda_ropa.cupon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidarCuponRequestDto {

    @NotBlank(message = "El código de cupón es requerido")
    private String codigo;
}
