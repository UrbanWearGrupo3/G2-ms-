package com.grupo3.tienda_ropa.Resenias.dtos;

import java.time.LocalDateTime;


public class ActualizarReseniaDTO {

    private Long id;
    private String comentario;
    private LocalDateTime fecha;
    private Integer puntuacion;
    private Long productoId;

    public ActualizarReseniaDTO(Long id, String comentario,
                                LocalDateTime fecha, Long productoId) {
            this.id = id;
            this.comentario = comentario;
            this.fecha = fecha;
            this.productoId = productoId;
        }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Integer puntuacion) {
        this.puntuacion = puntuacion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

}
