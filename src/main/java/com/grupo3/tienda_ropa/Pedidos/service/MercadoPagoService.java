package com.grupo3.tienda_ropa.Pedidos.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.back-urls.success}")
    private String successUrl;

    @Value("${mercadopago.back-urls.pending}")
    private String pendingUrl;

    @Value("${mercadopago.back-urls.failure}")
    private String failureUrl;

    public Preference crearPreferenciaDePago(Pedido pedido) {
        try {
            PreferenceClient client = new PreferenceClient();

            List<PreferenceItemRequest> items = pedido.getDetalles().stream()
                    .map(detalle -> PreferenceItemRequest.builder()
                            .id(detalle.getProducto().getId().toString())
                            .title(detalle.getProducto().getNombre())
                            .description(detalle.getProducto().getDescripcion())
                            .quantity(detalle.getCantidad())
                            .unitPrice(detalle.getProducto().getPrecio())
                            .build())
                    .collect(Collectors.toList());

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(pedido.getId().toString())
                    .build();

            return client.create(request);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la preferencia de Mercado Pago: " + e.getMessage(), e);
        }
    }
}
