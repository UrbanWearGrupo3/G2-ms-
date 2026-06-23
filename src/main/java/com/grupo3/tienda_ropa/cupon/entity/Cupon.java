package com.grupo3.tienda_ropa.cupon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupones")
@Getter
@Setter
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_descuento", nullable = false)
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "monto_minimo")
    private BigDecimal montoMinimo;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "limite_uso")
    private Integer limiteUso;

    @Column(name = "veces_usado", nullable = false)
    private Integer vecesUsado = 0;

    @Column(name = "permite_multiples_usos_por_cliente", nullable = false)
    private Boolean permiteMultiplesUsosPorCliente = false;
}
