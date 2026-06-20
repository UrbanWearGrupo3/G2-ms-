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
public class OcaEnvioAdapter implements EnvioStrategy {

    @Override
    public String getProveedor() {
        return "OCA";
    }

    @Override
    public CotizacionResultDto cotizarEnvio(Pedido pedido, String direccionDestino) {
        int cantidadItems = pedido.getDetalles() != null ? pedido.getDetalles().size() : 1;
        // Costos típicos de OCA en pesos argentinos (ARS)
        BigDecimal costoBase = new BigDecimal("5200.00");
        BigDecimal costoAdicional = new BigDecimal("950.00").multiply(BigDecimal.valueOf(cantidadItems));
        BigDecimal costoTotal = costoBase.add(costoAdicional);

        return CotizacionResultDto.builder()
                .proveedor(getProveedor())
                .costo(costoTotal)
                .diasEstimadosEntrega(3)
                .descripcionServicio("OCA Envíos Prioritarios a Domicilio")
                .build();
    }

    @Override
    public EnvioResultDto crearEnvio(Pedido pedido, String direccionDestino) {
        CotizacionResultDto cotizacion = cotizarEnvio(pedido, direccionDestino);
        
        String trackingCode = "OCA-" + UUID.randomUUID().toString().substring(0, 9).toUpperCase();
        
        return EnvioResultDto.builder()
                .proveedor(getProveedor())
                .codigoSeguimiento(trackingCode)
                .costo(cotizacion.getCosto())
                .estadoInicial("PREPARANDO")
                .etiquetaBase64("JVBERi0xLjQKJSDi48VyCg== (Mock OCA Label)")
                .build();
    }

    @Override
    public String consultarSeguimiento(String codigoSeguimiento, EnvioEstado estado) {
        return switch (estado) {
            case PREPARANDO -> "Estado OCA (Código: " + codigoSeguimiento + "): El remitente está preparando el paquete para ser entregado a OCA.";
            case EN_TRANSITO -> "Estado OCA (Código: " + codigoSeguimiento + "): Envío en tránsito local hacia la sucursal de destino en tu localidad.";
            case ENTREGADO -> "Estado OCA (Código: " + codigoSeguimiento + "): Entregado: El paquete ha sido entregado en el domicilio del destinatario.";
            case FALLIDO -> "Estado OCA (Código: " + codigoSeguimiento + "): Falla de entrega: Dirección no encontrada o destinatario ausente. El paquete retornará a sucursal.";
        };
    }
}
