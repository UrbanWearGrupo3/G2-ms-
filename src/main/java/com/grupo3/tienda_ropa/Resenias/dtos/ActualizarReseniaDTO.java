package com.grupo3.tienda_ropa.Resenias.dtos;

public class ActualizarReseniaDTO {

    private String comentario;
    private Integer puntuacion;

    public ActualizarReseniaDTO() {
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
}