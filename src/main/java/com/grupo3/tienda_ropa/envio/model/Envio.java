package com.grupo3.tienda_ropa.envio.model;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
@Getter
@Setter
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "proveedor_envio", nullable = false)
    private String proveedorEnvio;

    @Column(name = "codigo_seguimiento")
    private String codigoSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnvioEstado estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;

    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EnvioEstado.PREPARANDO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
