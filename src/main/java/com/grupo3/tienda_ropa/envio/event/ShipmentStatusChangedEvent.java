package com.grupo3.tienda_ropa.envio.event;

import com.grupo3.tienda_ropa.envio.model.Envio;

public record ShipmentStatusChangedEvent(Envio envio) {
}
