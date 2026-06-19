package com.grupo3.tienda_ropa.carrito.controllers;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.service.CarritoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carritos/{carritoId}/items")
@RequiredArgsConstructor
public class CarritoItemController {

    private final CarritoItemService carritoItemService;
    //Cargar Carrito
    @PostMapping
    public ResponseEntity<CarritoItem> agregarProducto(
            @PathVariable Long carritoId,
            @RequestParam Long productoId,
            @RequestParam Integer cantidad
    ) {

        CarritoItem item = carritoItemService.agregarProducto(
                carritoId,
                productoId,
                cantidad
        );

        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<CarritoItem>> obtenerItems(
            @PathVariable Long carritoId
    ) {

        return ResponseEntity.ok(
                carritoItemService.obtenerItems(carritoId)
        );
    }

    @GetMapping("/{productoId}")
    public ResponseEntity<CarritoItem> obtenerItem(
            @PathVariable Long carritoId,
            @PathVariable Long productoId
    ) {

        return ResponseEntity.ok(
                carritoItemService.obtenerItem(
                        carritoId,
                        productoId
                )
        );
    }


    @PutMapping("/{productoId}/disminuir")
    public ResponseEntity<Void> disminuirCantidad(
            @PathVariable Long carritoId,
            @PathVariable Long productoId
    ) {

        carritoItemService.disminuirCantidad(
                carritoId,
                productoId
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productoId}")
    public ResponseEntity<CarritoItem> actualizarCantidad(
            @PathVariable Long carritoId,
            @PathVariable Long productoId,
            @RequestParam Integer cantidad
    ) {

        return ResponseEntity.ok(
                carritoItemService.actualizarCantidad(
                        carritoId,
                        productoId,
                        cantidad
                )
        );
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long carritoId,
            @PathVariable Long productoId
    ) {

        carritoItemService.eliminarProducto(
                carritoId,
                productoId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(
            @PathVariable Long carritoId
    ) {

        carritoItemService.vaciarCarrito(carritoId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> contarProductos(
            @PathVariable Long carritoId
    ) {

        return ResponseEntity.ok(
                carritoItemService.contarProductos(carritoId)
        );
    }

    @GetMapping("/{productoId}/exists")
    public ResponseEntity<Boolean> existeProducto(
            @PathVariable Long carritoId,
            @PathVariable Long productoId
    ) {

        return ResponseEntity.ok(
                carritoItemService.existeProducto(
                        carritoId,
                        productoId
                )
        );
    }
}