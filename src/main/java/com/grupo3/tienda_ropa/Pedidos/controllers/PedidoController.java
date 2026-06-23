package com.grupo3.tienda_ropa.Pedidos.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.service.PedidoService;
import com.grupo3.tienda_ropa.Pedidos.service.MercadoPagoService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    private final PedidoService pedidoService;
    private final MercadoPagoService mercadoPagoService;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostMapping("/pago/webhook")
    public ResponseEntity<Void> webhookPago(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {
        
        try {
            logger.info("📩 Webhook recibido: {}", payload);
            logger.info("🔐 Headers - signature: {}, requestId: {}", signature, requestId);

            if (!isValidMercadoPagoNotification(payload, signature, requestId)) {
                logger.warn("⚠️ Notificación inválida - posible ataque");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String type = (String) payload.get("type");
            String action = (String) payload.get("action");
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String paymentId = data != null ? (String) data.get("id") : null;

            if ("payment".equals(type) && paymentId != null) {
                procesarPago(paymentId);
                logger.info("✅ Pago {} procesado correctamente", paymentId);
            } else if ("payment".equals(type) && "updated".equals(action)) {
                procesarActualizacionPago(paymentId);
                logger.info("🔄 Pago {} actualizado correctamente", paymentId);
            } else {
                logger.warn("⚠️ Evento no manejado: type={}, action={}", type, action);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("❌ Error procesando webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void procesarPago(String paymentId) {
        try {
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.parseLong(paymentId));
            
            logger.info("💰 Estado del pago {}: {}", paymentId, payment.getStatus());
            
            String externalReference = payment.getExternalReference();
            if (externalReference == null || externalReference.isEmpty()) {
                logger.error("❌ External reference no encontrado para el pago {}", paymentId);
                return;
            }
            
            Long pedidoId = Long.parseLong(externalReference);
            String estadoPedido = mapearEstadoPago(payment.getStatus());
            Pedido pedido = pedidoService.actualizarEstado(pedidoId, estadoPedido);
            
            if (pedido != null) {
                logger.info("✅ Pedido {} actualizado a estado: {}", pedidoId, estadoPedido);
                logger.info("   Pago: {}, Método: {}", paymentId, payment.getPaymentTypeId());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error procesando pago {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Error procesando pago", e);
        }
    }

    private void procesarActualizacionPago(String paymentId) {
        try {
            procesarPago(paymentId);
        } catch (Exception e) {
            logger.error("❌ Error actualizando pago {}: {}", paymentId, e.getMessage(), e);
            throw new RuntimeException("Error actualizando pago", e);
        }
    }

    private String mapearEstadoPago(String statusMercadoPago) {
        switch (statusMercadoPago) {
            case "approved":
                return "PAGADO";
            case "pending":
                return "PENDIENTE_PAGO";
            case "in_process":
                return "PROCESANDO";
            case "rejected":
                return "RECHAZADO";
            case "refunded":
                return "REEMBOLSADO";
            case "cancelled":
                return "CANCELADO";
            case "chargeback":
                return "CONTRACARGO";
            default:
                logger.warn("⚠️ Estado no mapeado: {}", statusMercadoPago);
                return "DESCONOCIDO";
        }
    }

    private boolean isValidMercadoPagoNotification(
            Map<String, Object> payload, 
            String signature, 
            String requestId) {
        
        if (signature == null || signature.isEmpty()) {
            logger.warn("⚠️ Notificación sin firma - podría ser prueba");
            return true; 
        }
        return false;
    }

    // ==================== CONFIRMAR PEDIDO ====================
    
    @PostMapping("/confirmar")
    public ResponseEntity<Pedido> confirmarPedido() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Pedido pedido = pedidoService.confirmarPedidoPorEmail(email);
        
        return ResponseEntity.ok(pedido);
    }

    // ==================== OBTENER PEDIDOS ====================

    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidos() {
        return ResponseEntity.ok(pedidoService.obtenerTodosLosPedidos());
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> obtenerMisPedidos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // 2. Obtener ID del usuario por email
        Long usuarioId = pedidoService.obtenerUsuarioIdPorEmail(email);
        
        // 3. Obtener pedidos del usuario
        return ResponseEntity.ok(pedidoService.obtenerPedidosUsuario(usuarioId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorEstado(estado));
    }

    @GetMapping("/mis-pedidos/estado/{estado}")
    public ResponseEntity<List<Pedido>> obtenerMisPedidosPorEstado(@PathVariable String estado) {
        // 1. Obtener email del token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        // 2. Obtener ID del usuario por email
        Long usuarioId = pedidoService.obtenerUsuarioIdPorEmail(email);
        
        // 3. Obtener pedidos del usuario por estado
        return ResponseEntity.ok(pedidoService.obtenerPedidosUsuarioPorEstado(usuarioId, estado));
    }

    // ==================== ACTUALIZAR ESTADO ====================

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        Pedido pedidoActualizado = pedidoService.actualizarEstado(id, estado);
        return ResponseEntity.ok(pedidoActualizado);
    }

    // ==================== INICIAR PAGO ====================

    @PostMapping("/{id}/pagar")
    public ResponseEntity<Map<String, String>> iniciarPago(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        Preference preference = mercadoPagoService.crearPreferenciaDePago(pedido);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("preferenceId", preference.getId());
        respuesta.put("initPoint", preference.getInitPoint());
        respuesta.put("sandboxInitPoint", preference.getSandboxInitPoint());

        return ResponseEntity.ok(respuesta);
    }

    // ==================== CALLBACKS DE PAGO ====================

    @GetMapping("/pago/success")
    public ResponseEntity<Map<String, Object>> pagoExitoso(
            @RequestParam("payment_id") String paymentId,
            @RequestParam("status") String status,
            @RequestParam("external_reference") Long pedidoId) {

        // Actualizar estado del pedido a PAGADO
        pedidoService.actualizarEstado(pedidoId, "PAGADO");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago aprobado con éxito");
        respuesta.put("estado", "APROBADO");
        respuesta.put("pedidoId", pedidoId);
        respuesta.put("paymentId", paymentId);
        respuesta.put("status", status);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/pago/pending")
    public ResponseEntity<Map<String, Object>> pagoPendiente(
            @RequestParam("payment_id") String paymentId,
            @RequestParam("status") String status,
            @RequestParam("external_reference") Long pedidoId) {

        // Actualizar estado del pedido a PENDIENTE_PAGO
        pedidoService.actualizarEstado(pedidoId, "PENDIENTE_PAGO");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago pendiente de procesamiento");
        respuesta.put("estado", "PENDIENTE_PAGO");
        respuesta.put("pedidoId", pedidoId);
        respuesta.put("paymentId", paymentId);
        respuesta.put("status", status);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/pago/failure")
    public ResponseEntity<Map<String, Object>> pagoFallido(
            @RequestParam("external_reference") Long pedidoId) {

        // Actualizar estado del pedido a RECHAZADO
        pedidoService.actualizarEstado(pedidoId, "RECHAZADO");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago rechazado o fallido");
        respuesta.put("estado", "RECHAZADO");
        respuesta.put("pedidoId", pedidoId);

        return ResponseEntity.ok(respuesta);
    }

  
    }
