package com.grupo3.tienda_ropa.envio.strategy;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.envio.dto.CotizacionResultDto;
import com.grupo3.tienda_ropa.envio.dto.EnvioResultDto;

import com.grupo3.tienda_ropa.envio.model.EnvioEstado;

public interface EnvioStrategy {

    /**
     * Devuelve el nombre del proveedor que implementa esta estrategia.
     * Ejemplo: "DHL", "FEDEX", "ESTAFETA", "LOCAL"
     */
    String getProveedor();

    /**
     * Realiza una cotización de costo y tiempo de entrega.
     */
    CotizacionResultDto cotizarEnvio(Pedido pedido, String direccionDestino);

    /**
     * Registra el envío en la plataforma del proveedor y retorna los datos resultantes.
     */
    EnvioResultDto crearEnvio(Pedido pedido, String direccionDestino);

    /**
     * Consulta el estado actual de tracking del paquete usando el código de seguimiento.
     */
    String consultarSeguimiento(String codigoSeguimiento, EnvioEstado estado);
}
