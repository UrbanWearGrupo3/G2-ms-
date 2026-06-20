package com.grupo3.tienda_ropa.envio.event;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.envio.model.Envio;
import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import com.grupo3.tienda_ropa.notification.model.NotificationType;
import com.grupo3.tienda_ropa.notification.service.EmailNotificationService;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShipmentNotificationListener {

    private final EmailNotificationService emailNotificationService;

    @EventListener
    public void handleShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        Envio envio = event.envio();
        Pedido pedido = envio.getPedido();
        if (pedido == null) {
            return;
        }

        Usuario usuario = pedido.getUsuario();
        if (usuario == null || usuario.getEmail() == null) {
            return;
        }

        // Construir datos del template
        Map<String, Object> templateData = new HashMap<>();
        String fullName = usuario.getNombre() + " " + usuario.getApellido();
        templateData.put("clientName", fullName);
        templateData.put("carrierName", envio.getProveedorEnvio());
        templateData.put("trackingCode", envio.getCodigoSeguimiento());
        templateData.put("destinationAddress", envio.getDireccionDestino());
        
        // Traducir estado a algo amigable para el cliente
        String estadoAmigable = switch (envio.getEstado()) {
            case PREPARANDO -> "PREPARANDO ENVÍO";
            case EN_TRANSITO -> "EN TRÁNSITO / EN CAMINO";
            case ENTREGADO -> "ENTREGADO EXITOSAMENTE";
            case FALLIDO -> "FALLIDO / RETENIDO EN ADUANA/DISTRIBUCIÓN";
        };
        templateData.put("shippingStatus", estadoAmigable);

        // Opcional: Tiempo estimado
        String estimate = switch (envio.getProveedorEnvio().toUpperCase()) {
            case "CORREO_ARGENTINO" -> "3 a 6 días hábiles (Estándar)";
            case "OCA" -> "2 a 4 días hábiles (Prioritario)";
            case "ANDREANI" -> "1 a 3 días hábiles (Express)";
            default -> "3 a 5 días hábiles";
        };
        templateData.put("deliveryEstimate", estimate);
        templateData.put("trackingUrl", "https://www.urbanwear.com/tracking?code=" + envio.getCodigoSeguimiento());

        // Crear la petición de notificación
        NotificationRequest request = NotificationRequest.builder()
                .recipient(usuario.getEmail())
                .subject("Actualización de tu Envío para el Pedido #" + pedido.getId() + " [" + envio.getEstado() + "]")
                .type(NotificationType.SHIPPING_UPDATE)
                .templateData(templateData)
                .build();

        // Enviar correo de forma asíncrona
        emailNotificationService.sendEmail(request);
    }
}
