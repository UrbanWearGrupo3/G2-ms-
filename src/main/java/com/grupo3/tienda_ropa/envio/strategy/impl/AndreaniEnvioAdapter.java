package com.grupo3.tienda_ropa.envio.strategy.impl;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.envio.dto.CotizacionResultDto;
import com.grupo3.tienda_ropa.envio.dto.EnvioResultDto;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

import com.grupo3.tienda_ropa.envio.model.EnvioEstado;

@Component
public class AndreaniEnvioAdapter implements EnvioStrategy {

    @Override
    public String getProveedor() {
        return "ANDREANI";
    }

    @Override
    public CotizacionResultDto cotizarEnvio(Pedido pedido, String direccionDestino) {
        int cantidadItems = pedido.getDetalles() != null ? pedido.getDetalles().size() : 1;
        // Costos típicos de Andreani en pesos argentinos (ARS)
        BigDecimal costoBase = new BigDecimal("6000.00");
        BigDecimal costoAdicional = new BigDecimal("1100.00").multiply(BigDecimal.valueOf(cantidadItems));
        BigDecimal costoTotal = costoBase.add(costoAdicional);

        // Envío gratuito si la compra supera los 80,000 pesos argentinos (ARS)
        if (pedido.getTotal() != null && pedido.getTotal().compareTo(new BigDecimal("80000.00")) >= 0) {
            costoTotal = BigDecimal.ZERO;
        }

        return CotizacionResultDto.builder()
                .proveedor(getProveedor())
                .costo(costoTotal)
                .diasEstimadosEntrega(2)
                .descripcionServicio("Andreani Express Nacional - Envío Urgente")
                .build();
    }

    @Override
    public EnvioResultDto crearEnvio(Pedido pedido, String direccionDestino) {
        CotizacionResultDto cotizacion = cotizarEnvio(pedido, direccionDestino);
        
        String trackingCode = "AND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        return EnvioResultDto.builder()
                .proveedor(getProveedor())
                .codigoSeguimiento(trackingCode)
                .costo(cotizacion.getCosto())
                .estadoInicial("PREPARANDO")
                .etiquetaBase64("JVBERi0xLjQKJSDi48VyCg== (Mock Andreani Label)")
                .build();
    }

    @Override
    public String consultarSeguimiento(String codigoSeguimiento, EnvioEstado estado) {
        return switch (estado) {
            case PREPARANDO -> "Estado Andreani (Código: " + codigoSeguimiento + "): Andreani está a la espera de recibir el paquete por parte del remitente.";
            case EN_TRANSITO -> "Estado Andreani (Código: " + codigoSeguimiento + "): El envío se encuentra en camino. La unidad de reparto está en ruta hacia el domicilio.";
            case ENTREGADO -> "Estado Andreani (Código: " + codigoSeguimiento + "): Entregado: Envío entregado con firma digital en destino.";
            case FALLIDO -> "Estado Andreani (Código: " + codigoSeguimiento + "): No entregado: Visita fallida por domicilio cerrado. Coordinar retiro en sucursal.";
        };
    }
}
