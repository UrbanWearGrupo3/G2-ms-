package com.grupo3.tienda_ropa.Pedidos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pedidos")
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "cupon_codigo")
    private String cuponCodigo;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "external_reference")
    private String externalReference;


    private String direccionEnvio;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("pedido") // ✅ Rompe el bucle
    private List<PedidosDetalles> detalles;
}