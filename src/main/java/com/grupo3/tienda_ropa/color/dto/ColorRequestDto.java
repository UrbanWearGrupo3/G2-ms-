package com.grupo3.tienda_ropa.color.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorRequestDto {

    @NotBlank(message = "El nombre del color es obligatorio")
    private String nombre;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El código hexadecimal debe tener el formato #RRGGBB")
    private String codigoHex;
}
