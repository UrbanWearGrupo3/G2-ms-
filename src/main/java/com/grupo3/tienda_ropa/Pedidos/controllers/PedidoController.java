package com.grupo3.tienda_ropa.Pedidos.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.service.PedidoService;
import com.grupo3.tienda_ropa.Pedidos.service.MercadoPagoService;
import com.mercadopago.resources.preference.Preference;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final MercadoPagoService mercadoPagoService;

    @PostMapping("/confirmar")
    public ResponseEntity<Pedido> confirmarPedido() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long usuarioId = Long.parseLong(authentication.getName());

        Pedido pedido = pedidoService.confirmarPedido(usuarioId);

        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> obtenerTodosLosPedidos() {
        
        return ResponseEntity.ok(
                pedidoService.obtenerTodosLosPedidos()
        );
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> obtenerMisPedidos() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long usuarioId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                pedidoService.obtenerPedidosUsuario(usuarioId)
        );
    }
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Pedido>> obtenerPedidosPorEstado(
            @PathVariable String estado
    ) {

        return ResponseEntity.ok(
                pedidoService.obtenerPedidosPorEstado(estado)
        );
    }

    @GetMapping("/mis-pedidos/estado/{estado}")
    public ResponseEntity<List<Pedido>> obtenerMisPedidosPorEstado(
            @PathVariable String estado
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long usuarioId = Long.parseLong(authentication.getName());

        return ResponseEntity.ok(
                pedidoService.obtenerPedidosUsuarioPorEstado(
                        usuarioId,
                        estado
                )
        );
    }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        Pedido pedidoActualizado = pedidoService.actualizarEstado(id, estado);

        return ResponseEntity.ok(pedidoActualizado);
    }

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

    @GetMapping("/pago/success")
    public ResponseEntity<Map<String, Object>> pagoExitoso(
            @RequestParam("payment_id") String paymentId,
            @RequestParam("status") String status,
            @RequestParam("external_reference") Long pedidoId) {

        Pedido pedido = pedidoService.actualizarEstado(pedidoId, "APROBADO");

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

        Pedido pedido = pedidoService.actualizarEstado(pedidoId, "PENDIENTE_PAGO");

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

        Pedido pedido = pedidoService.actualizarEstado(pedidoId, "RECHAZADO");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Pago rechazado o fallido");
        respuesta.put("estado", "RECHAZADO");
        respuesta.put("pedidoId", pedidoId);

        return ResponseEntity.ok(respuesta);
    }
}