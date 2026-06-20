package com.grupo3.tienda_ropa.envio.dto;

import com.grupo3.tienda_ropa.envio.model.Envio;
import com.grupo3.tienda_ropa.envio.model.EnvioEstado;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnvioResponse {
    private Long id;
    private Long pedidoId;
    private String proveedorEnvio;
    private String codigoSeguimiento;
    private EnvioEstado estado;
    private BigDecimal costo;
    private String direccionDestino;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public static EnvioResponse fromEntity(Envio envio) {
        EnvioResponse response = new EnvioResponse();
        response.setId(envio.getId());
        if (envio.getPedido() != null) {
            response.setPedidoId(envio.getPedido().getId());
        }
        response.setProveedorEnvio(envio.getProveedorEnvio());
        response.setCodigoSeguimiento(envio.getCodigoSeguimiento());
        response.setEstado(envio.getEstado());
        response.setCosto(envio.getCosto());
        response.setDireccionDestino(envio.getDireccionDestino());
        response.setFechaCreacion(envio.getFechaCreacion());
        response.setFechaActualizacion(envio.getFechaActualizacion());
        return response;
    }
}
