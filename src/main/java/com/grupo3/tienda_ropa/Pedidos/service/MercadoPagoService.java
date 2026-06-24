package com.grupo3.tienda_ropa.Pedidos.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceRequest.PreferenceRequestBuilder;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import jakarta.annotation.PostConstruct;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

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
        // Loguear solo los primeros/últimos 4 chars del token para diagnóstico sin exponer el secreto
        String tokenPreview = accessToken != null && accessToken.length() > 8
                ? accessToken.substring(0, 4) + "..." + accessToken.substring(accessToken.length() - 4)
                : "(vacío)";
        logger.info("✅ Mercado Pago configurado. Token: {}", tokenPreview);
    }

    public Preference crearPreferenciaDePago(Pedido pedido) {
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

            List<PreferenceItemRequest> items = pedido.getDetalles().stream()
                    .map(detalle -> {
                        if (detalle.getProducto() == null) {
                            throw new IllegalArgumentException("Detalle con producto null");
                        }

                        BigDecimal precio = detalle.getProducto().getPrecio();
                        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
                            precio = BigDecimal.ONE; // MP no acepta precio 0
                        }

                        return PreferenceItemRequest.builder()
                                .id(detalle.getProducto().getId().toString())
                                .title(detalle.getProducto().getNombre() != null
                                        ? detalle.getProducto().getNombre()
                                        : "Producto sin nombre")
                                .description(detalle.getProducto().getDescripcion() != null
                                        ? detalle.getProducto().getDescripcion()
                                        : "")
                                .currencyId("ARS")  // Requerido por la API de MP
                                .quantity(detalle.getCantidad() > 0 ? detalle.getCantidad() : 1)
                                .unitPrice(precio)
                                .build();
                    })
                    .collect(Collectors.toList());

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(successUrl)
                    .pending(pendingUrl)
                    .failure(failureUrl)
                    .build();

            PreferenceRequestBuilder requestBuilder = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(pedido.getId().toString());

            if (notificationUrl != null && !notificationUrl.isEmpty()) {
                requestBuilder.notificationUrl(notificationUrl);
            }

            PreferenceRequest request = requestBuilder.build();
            logger.info("📤 Enviando preferencia a MP para pedido #{}, items: {}", pedido.getId(), items.size());

            Preference preference = client.create(request);
            logger.info("✅ Preferencia creada: id={}, initPoint={}", preference.getId(), preference.getInitPoint());
            return preference;

        } catch (MPApiException e) {
            // Loguear la respuesta real de la API de Mercado Pago
            String responseBody = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            logger.error("❌ Error de API Mercado Pago — HTTP {}: {}", e.getStatusCode(), responseBody);
            throw new RuntimeException(
                    "Error de Mercado Pago (HTTP " + e.getStatusCode() + "): " + responseBody, e);
        } catch (Exception e) {
            logger.error("❌ Error inesperado al crear preferencia MP: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la preferencia de Mercado Pago: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los detalles de un pago desde la API de Mercado Pago.
     * Centraliza el uso de PaymentClient para evitar instanciarlo en el controller.
     */
    public Payment obtenerPago(String paymentId) {
        try {
            PaymentClient paymentClient = new PaymentClient();
            return paymentClient.get(Long.parseLong(paymentId));
        } catch (MPApiException e) {
            String responseBody = e.getApiResponse() != null ? e.getApiResponse().getContent() : e.getMessage();
            logger.error("❌ Error de API MP al obtener pago {} — HTTP {}: {}", paymentId, e.getStatusCode(), responseBody);
            throw new RuntimeException(
                    "Error de Mercado Pago (HTTP " + e.getStatusCode() + "): " + responseBody, e);
        } catch (Exception e) {
            logger.error("❌ Error inesperado al obtener pago {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener el pago " + paymentId + ": " + e.getMessage(), e);
        }
    }
}