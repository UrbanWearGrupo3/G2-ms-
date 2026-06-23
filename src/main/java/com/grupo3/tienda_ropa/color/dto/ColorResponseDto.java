package com.grupo3.tienda_ropa.color.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ColorResponseDto {
    private Long id;
    private String nombre;
    private String codigoHex;
    private Boolean activo;
}
