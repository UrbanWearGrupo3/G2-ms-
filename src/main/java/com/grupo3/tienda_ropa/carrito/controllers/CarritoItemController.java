package com.grupo3.tienda_ropa.carrito.controllers;

import com.grupo3.tienda_ropa.carrito.dtos.CarritoItemRequest;
import com.grupo3.tienda_ropa.carrito.dtos.CarritoItemResponse;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.service.CarritoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/carritos/{carritoId}/items")
@RequiredArgsConstructor
public class CarritoItemController {

        private final CarritoItemService carritoItemService;

        // Cargar Carrito
        @PostMapping
        public ResponseEntity<CarritoItemResponse> agregarProducto(
                        @PathVariable Long carritoId,
                        @RequestBody CarritoItemRequest request) {

                CarritoItem item = carritoItemService.agregarProducto(
                                carritoId,
                                request.getProductoId(),
                                request.getCantidad());

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
                                                .multiply(BigDecimal.valueOf(item.getCantidad())));

                return response;
        }

        @GetMapping
        public ResponseEntity<List<CarritoItemResponse>> obtenerItems(
                        @PathVariable Long carritoId) {

                List<CarritoItemResponse> items = carritoItemService.obtenerItems(carritoId)
                                .stream()
                                .map(this::convertirResponse)
                                .toList();

                return ResponseEntity.ok(items);
        }

        @GetMapping("/{productoId}")
        public ResponseEntity<CarritoItemResponse> obtenerItem(
                        @PathVariable Long carritoId,
                        @PathVariable Long productoId) {

                CarritoItem item = carritoItemService.obtenerItem(
                                carritoId,
                                productoId);

                return ResponseEntity.ok(
                                convertirResponse(item));
        }

        @PutMapping("/{productoId}/disminuir")
        public ResponseEntity<Void> disminuirCantidad(
                        @PathVariable Long carritoId,
                        @PathVariable Long productoId) {

                carritoItemService.disminuirCantidad(
                                carritoId,
                                productoId);

                return ResponseEntity.noContent().build();
        }

        @PutMapping("/{productoId}")
        public ResponseEntity<CarritoItemResponse> actualizarCantidad(
                        @PathVariable Long carritoId,
                        @PathVariable Long productoId,
                        @RequestParam Integer cantidad) {

                CarritoItem item = carritoItemService.actualizarCantidad(
                                carritoId,
                                productoId,
                                cantidad);

                return ResponseEntity.ok(
                                convertirResponse(item));
        }

        @DeleteMapping("/{productoId}")
        public ResponseEntity<Void> eliminarProducto(
                        @PathVariable Long carritoId,
                        @PathVariable Long productoId) {

                carritoItemService.eliminarProducto(
                                carritoId,
                                productoId);

                return ResponseEntity.noContent().build();
        }

        @DeleteMapping
        public ResponseEntity<Void> vaciarCarrito(
                        @PathVariable Long carritoId) {

                carritoItemService.vaciarCarrito(carritoId);

                return ResponseEntity.noContent().build();
        }

        @GetMapping("/count")
        public ResponseEntity<Long> contarProductos(
                        @PathVariable Long carritoId) {

                return ResponseEntity.ok(
                                carritoItemService.contarProductos(carritoId));
        }

        @GetMapping("/{productoId}/exists")
        public ResponseEntity<Boolean> existeProducto(
                        @PathVariable Long carritoId,
                        @PathVariable Long productoId) {

                return ResponseEntity.ok(
                                carritoItemService.existeProducto(
                                                carritoId,
                                                productoId));
        }
}