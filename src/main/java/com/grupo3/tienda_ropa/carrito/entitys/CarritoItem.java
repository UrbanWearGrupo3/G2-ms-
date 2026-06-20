package com.grupo3.tienda_ropa.carrito.entitys;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "carrito_items")
@Getter
@Setter
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "carrito_id")
@JsonIgnore
private CarritoEntity carrito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

  
}