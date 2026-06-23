package com.grupo3.tienda_ropa.envio.controller;

import com.grupo3.tienda_ropa.envio.dto.CotizacionResultDto;
import com.grupo3.tienda_ropa.envio.dto.CrearEnvioRequest;
import com.grupo3.tienda_ropa.envio.dto.EnvioResponse;
import com.grupo3.tienda_ropa.envio.model.Envio;
import com.grupo3.tienda_ropa.envio.model.EnvioEstado;
import com.grupo3.tienda_ropa.envio.service.EnvioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping("/proveedores")
    public ResponseEntity<List<String>> obtenerProveedoresSoportados() {
        return ResponseEntity.ok(envioService.obtenerProveedoresSoportados());
    }

    @GetMapping("/cotizar")
    public ResponseEntity<CotizacionResultDto> cotizarEnvio(
            @RequestParam Long pedidoId,
            @RequestParam String proveedor,
            @RequestParam String direccionDestino,
            Authentication authentication) {
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        boolean isAdminOrSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
        envioService.validarAccesoPedido(pedidoId, usuarioLogueado, isAdminOrSuper);
        return ResponseEntity.ok(envioService.cotizarEnvio(pedidoId, proveedor, direccionDestino));
    }

    @PostMapping
    public ResponseEntity<EnvioResponse> crearEnvio(
            @Valid @RequestBody CrearEnvioRequest request,
            Authentication authentication) {
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        boolean isAdminOrSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
        envioService.validarAccesoPedido(request.getPedidoId(), usuarioLogueado, isAdminOrSuper);
        Envio envio = envioService.crearEnvio(
                request.getPedidoId(),
                request.getProveedor(),
                request.getDireccionDestino()
        );
        return ResponseEntity.ok(EnvioResponse.fromEntity(envio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponse> obtenerEnvioPorId(
            @PathVariable Long id,
            Authentication authentication) {
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        boolean isAdminOrSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
        envioService.validarAccesoEnvio(id, usuarioLogueado, isAdminOrSuper);
        Envio envio = envioService.obtenerEnvioPorId(id);
        return ResponseEntity.ok(EnvioResponse.fromEntity(envio));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EnvioResponse> obtenerEnvioPorPedidoId(
            @PathVariable Long pedidoId,
            Authentication authentication) {
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        boolean isAdminOrSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
        envioService.validarAccesoPedido(pedidoId, usuarioLogueado, isAdminOrSuper);
        Envio envio = envioService.obtenerEnvioPorPedidoId(pedidoId);
        return ResponseEntity.ok(EnvioResponse.fromEntity(envio));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<EnvioResponse> actualizarEstadoEnvio(
            @PathVariable Long id,
            @RequestParam EnvioEstado nuevoEstado) {
        Envio envio = envioService.actualizarEstadoEnvio(id, nuevoEstado);
        return ResponseEntity.ok(EnvioResponse.fromEntity(envio));
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<String> consultarTracking(
            @PathVariable Long id,
            Authentication authentication) {
        Usuario usuarioLogueado = (Usuario) authentication.getPrincipal();
        boolean isAdminOrSuper = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
        envioService.validarAccesoEnvio(id, usuarioLogueado, isAdminOrSuper);
        String infoTracking = envioService.consultarTrackingDesdeProveedor(id);
        return ResponseEntity.ok(infoTracking);
    }
}
