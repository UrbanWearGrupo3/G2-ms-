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
public class CorreoArgentinoEnvioAdapter implements EnvioStrategy {

    @Override
    public String getProveedor() {
        return "CORREO_ARGENTINO";
    }

    @Override
    public CotizacionResultDto cotizarEnvio(Pedido pedido, String direccionDestino) {
        int cantidadItems = pedido.getDetalles() != null ? pedido.getDetalles().size() : 1;
        // Costos típicos de Correo Argentino en pesos argentinos (ARS)
        BigDecimal costoBase = new BigDecimal("4500.00");
        BigDecimal costoAdicional = new BigDecimal("800.00").multiply(BigDecimal.valueOf(cantidadItems));
        BigDecimal costoTotal = costoBase.add(costoAdicional);

        return CotizacionResultDto.builder()
                .proveedor(getProveedor())
                .costo(costoTotal)
                .diasEstimadosEntrega(5)
                .descripcionServicio("Correo Argentino Clásico - Envíos Nacionales")
                .build();
    }

    @Override
    public EnvioResultDto crearEnvio(Pedido pedido, String direccionDestino) {
        CotizacionResultDto cotizacion = cotizarEnvio(pedido, direccionDestino);
        
        String trackingCode = "SD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-AR";
        
        return EnvioResultDto.builder()
                .proveedor(getProveedor())
                .codigoSeguimiento(trackingCode)
                .costo(cotizacion.getCosto())
                .estadoInicial("PREPARANDO")
                .etiquetaBase64("JVBERi0xLjQKJSDi48VyCg== (Mock Correo Argentino Label)")
                .build();
    }

    @Override
    public String consultarSeguimiento(String codigoSeguimiento, EnvioEstado estado) {
        return switch (estado) {
            case PREPARANDO -> "Estado Correo Argentino (Código: " + codigoSeguimiento + "): Pre-ingreso: El remitente ha registrado el envío pero aún no ha sido entregado en la sucursal de Correo Argentino.";
            case EN_TRANSITO -> "Estado Correo Argentino (Código: " + codigoSeguimiento + "): En tránsito: El paquete ingresó a la planta de procesamiento nacional en Buenos Aires.";
            case ENTREGADO -> "Estado Correo Argentino (Código: " + codigoSeguimiento + "): Entregado: Envío entregado a la persona autorizada.";
            case FALLIDO -> "Estado Correo Argentino (Código: " + codigoSeguimiento + "): Intento de entrega fallido: El cartero no pudo concretar la entrega. Se realizará un nuevo intento.";
        };
    }
}
