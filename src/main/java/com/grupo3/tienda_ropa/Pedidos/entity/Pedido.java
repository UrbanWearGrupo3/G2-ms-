package com.grupo3.tienda_ropa.Pedidos.entity;

import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    private String estado;

    private BigDecimal total;

    private String direccionEnvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetallePedidoEntity> detalles;

    @PrePersist
    public void prePersist() {
        fecha = LocalDateTime.now();
    }
}