package com.grupo3.tienda_ropa.Pedidos.controllers;

import com.grupo3.tienda_ropa.Pedidos.deto.PedidoResponse;
import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
public class PedidosControllers {

    private final PedidoService pedidoService;

    // Solo ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> obtenerTodos() {
        return ResponseEntity.ok(
                pedidoService.obtenerTodos()
        );
    }

    // Usuario autenticado
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResponse>> obtenerMisPedidos(
            Principal principal
    ) {
        return ResponseEntity.ok(
                pedidoService.obtenerMisPedidos(
                        principal.getName()
                )
        );
    }

    // Solo ADMIN
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Pedido>> obtenerPorEstado(
            @PathVariable String estado
    ) {
        return ResponseEntity.ok(
                pedidoService.obtenerPedidosPorEstado(estado)
        );
    }

    // Usuario autenticado
    @GetMapping("/mis-pedidos/estado/{estado}")
    public ResponseEntity<List<Pedido>> obtenerMisPedidosPorEstado(
            @PathVariable String estado,
            Principal principal
    ) {
        return ResponseEntity.ok(
                pedidoService.obtenerMisPedidosPorEstado(
                        principal.getName(),
                        estado
                )
        );
    }
}