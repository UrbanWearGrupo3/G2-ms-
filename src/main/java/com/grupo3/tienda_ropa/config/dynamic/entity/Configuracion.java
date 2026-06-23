package com.grupo3.tienda_ropa.config.dynamic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracion {
    @Id
    private String clave;

    @Column(nullable = false, length = 2048)
    private String valor;

    private String descripcion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por")
    private String actualizadoPor;
}
