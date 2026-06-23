package com.grupo3.tienda_ropa.config.dynamic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditorias_configuracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaConfiguracion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clave;

    @Column(name = "valor_anterior", length = 2048)
    private String valorAnterior;

    @Column(name = "valor_nuevo", length = 2048)
    private String valorNuevo;

    @Column(nullable = false)
    private String usuario;

    @Column(name = "tipo_accion", nullable = false)
    private String tipoAccion;

    @Column(name = "fecha_cambio", nullable = false)
    private LocalDateTime fechaCambio;
}
