package com.grupo3.tienda_ropa.usuario.deto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String token;
    private String email;
    private String rol;
    private String nombre;
    private String apellido;
}
