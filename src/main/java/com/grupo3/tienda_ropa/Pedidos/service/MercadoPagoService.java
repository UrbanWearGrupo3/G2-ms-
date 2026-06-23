package com.grupo3.tienda_ropa.Pedidos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceRequest.PreferenceRequestBuilder;
import com.mercadopago.resources.preference.Preference;

import jakarta.annotation.PostConstruct; 

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.back-urls.success}")
    private String successUrl;

    @Value("${mercadopago.back-urls.pending}")
    private String pendingUrl;

    @Value("${mercadopago.back-urls.failure}")
    private String failureUrl;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        System.out.println("✅ Mercado Pago configurado correctamente");
    }

    @Transactional(readOnly = true)
    public Preference crearPreferenciaDePago(Pedido pedido) {
        // Validaciones
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido no puede ser null");
        }

        if (pedido.getId() == null) {
            throw new IllegalArgumentException("El pedido debe tener un ID válido");
        }

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        try {
            PreferenceClient client = new PreferenceClient();

            // Crear items
            List<PreferenceItemRequest> items = pedido.getDetalles().stream()
                    .map(detalle -> {
                        if (detalle.getProducto() == null) {
                            throw new IllegalArgumentException("Detalle con producto null");
                        }

                        BigDecimal precio = detalle.getProducto().getPrecio();
                        if (precio == null) {
                            precio = BigDecimal.ZERO;
                        }

                        return PreferenceItemRequest.builder()
                                .id(detalle.getProducto().getId().toString())
                                .title(detalle.getProducto().getNombre() != null ? 
                                       detalle.getProducto().getNombre() : "Producto sin nombre")
                                .description(detalle.getProducto().getDescripcion())
                                .quantity(detalle.getCantidad() > 0 ? detalle.getCantidad() : 1)
                                .unitPrice(precio)
                                .build();
                    })
                    .collect(Collectors.toList());

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(pedido.getId().toString());

            // ✅ MEJORADO: Solo agregar notificationUrl si está configurada
            if (notificationUrl != null && !notificationUrl.isEmpty()) {
                requestBuilder.notificationUrl(notificationUrl);
            }

            PreferenceRequest request = requestBuilder.build();

            return client.create(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al crear la preferencia de Mercado Pago: " + e.getMessage(), e);
        }
    }
}