package com.grupo3.tienda_ropa.producto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grupo3.tienda_ropa.color.entity.Color;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variantes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"producto_id", "talle", "color_id"}),
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
    @JsonIgnore
    private Producto producto;

    @Column(nullable = false)
    private String talle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "codigo_barras")
    private String codigoBarras;

    @Column(nullable = false)
    private Boolean activo = true;
}
