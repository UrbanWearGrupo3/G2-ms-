package com.grupo3.tienda_ropa.producto.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variantes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"producto_id", "talle", "color"}),
    @UniqueConstraint(columnNames = {"codigo_barras"})
})
@Getter
@Setter
public class Variante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private String talle;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "codigo_barras")
    private String codigoBarras;
}
