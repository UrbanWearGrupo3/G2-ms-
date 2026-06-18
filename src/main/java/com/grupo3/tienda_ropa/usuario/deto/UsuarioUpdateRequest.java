package com.grupo3.tienda_ropa.usuario.deto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
