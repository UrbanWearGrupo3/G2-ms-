package com.grupo3.tienda_ropa.producto.entity;

import com.grupo3.tienda_ropa.categoria.entity.CatergoriaEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DetallesProductos")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @DecimalMax(value = "999999.99", message = "El precio no puede exceder 999999.99")
    private Double precio;  
    @NotBlank(message = "El talle es obligatorio")
    private String talle;    
    @NotBlank(message = "El color es obligatorio")
    private String color;
    private String imagen;
    //Validacion de estock    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 99999, message = "El stock no puede exceder 99999")
    private Integer stock; 
    // categoria
    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CatergoriaEntity categoria;
}