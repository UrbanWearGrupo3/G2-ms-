package com.grupo3.tienda_ropa.Pedidos.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.service.PedidoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

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
}