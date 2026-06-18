package com.grupo3.tienda_ropa.usuario.deto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
