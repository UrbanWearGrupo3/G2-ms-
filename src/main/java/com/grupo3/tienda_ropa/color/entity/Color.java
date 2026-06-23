package com.grupo3.tienda_ropa.color.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "colores")
@Getter
@Setter
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "codigo_hex", length = 7)
    private String codigoHex;

    @Column(nullable = false)
    private Boolean activo = true;
}
