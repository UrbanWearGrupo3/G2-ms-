package com.grupo3.tienda_ropa.carrito.controllers;

import com.grupo3.tienda_ropa.carrito.dtos.CarritoItemRequest;
import com.grupo3.tienda_ropa.carrito.dtos.CarritoItemResponse;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.service.CarritoItemService;
import com.grupo3.tienda_ropa.carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/carritos/{carritoId}/items")
@RequiredArgsConstructor
public class CarritoItemController {

    private final CarritoItemService carritoItemService;
    private final CarritoService carritoService;

    private void validarAccesoCarrito(Long carritoId, Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("No autenticado");
        }
        CarritoEntity carritoUsuario = carritoService.obtenerOCrearCarritoPorEmail(principal.getName());
        if (!carritoUsuario.getId().equals(carritoId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrSuper = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_USER"));
            if (!isAdminOrSuper) {
                throw new AccessDeniedException("No tienes permiso para acceder a este carrito");
            }
        }
    }

    //Cargar Carrito
    @PostMapping
    public ResponseEntity<CarritoItemResponse> agregarProducto(
            @PathVariable Long carritoId,
            @RequestBody CarritoItemRequest request,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        CarritoItem item = carritoItemService.agregarProducto(
                carritoId,
                request.getProductoId(),
                request.getCantidad()
        );

        return ResponseEntity.ok(convertirResponse(item));
    }

    private CarritoItemResponse convertirResponse(CarritoItem item) {

        CarritoItemResponse response = new CarritoItemResponse();

        response.setId(item.getId());
        response.setCantidad(item.getCantidad());

        response.setProductoId(item.getProducto().getId());
        response.setNombreProducto(item.getProducto().getNombre());
        response.setDescripcion(item.getProducto().getDescripcion());
        response.setImagenUrl(item.getProducto().getImagenUrl());
        response.setPrecio(item.getProducto().getPrecio());

        response.setVariantes(List.of());

        response.setSubtotal(
                item.getProducto()
                        .getPrecio()
                        .multiply(BigDecimal.valueOf(item.getCantidad()))
        );

        return response;
    }

    @GetMapping
    public ResponseEntity<List<CarritoItemResponse>> obtenerItems(
            @PathVariable Long carritoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        List<CarritoItemResponse> items =
                carritoItemService.obtenerItems(carritoId)
                        .stream()
                        .map(this::convertirResponse)
                        .toList();

        return ResponseEntity.ok(items);
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<CarritoItemResponse> obtenerItem(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        CarritoItem item = carritoItemService.obtenerItem(
                carritoId,
                productoId
        );

        return ResponseEntity.ok(
                convertirResponse(item)
        );
    }

    @PutMapping("/{productoId}/disminuir")
    public ResponseEntity<Void> disminuirCantidad(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        carritoItemService.disminuirCantidad(
                carritoId,
                productoId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productoId}")
    public ResponseEntity<CarritoItemResponse> actualizarCantidad(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            @RequestParam Integer cantidad,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        CarritoItem item =
                carritoItemService.actualizarCantidad(
                        carritoId,
                        productoId,
                        cantidad
                );

        return ResponseEntity.ok(
                convertirResponse(item)
        );
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        carritoItemService.eliminarProducto(
                carritoId,
                productoId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(
            @PathVariable Long carritoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        carritoItemService.vaciarCarrito(carritoId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarProductos(
            @PathVariable Long carritoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        return ResponseEntity.ok(
                carritoItemService.contarProductos(carritoId)
        );
    }

    @GetMapping("/{productoId}/exists")
    public ResponseEntity<Boolean> existeProducto(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            Principal principal
    ) {
        validarAccesoCarrito(carritoId, principal);

        return ResponseEntity.ok(
                carritoItemRepoExists(carritoId, productoId)
        );
    }

    private Boolean carritoItemRepoExists(Long carritoId, Long productoId) {
        return carritoItemService.existeProducto(carritoId, productoId);
    }
}