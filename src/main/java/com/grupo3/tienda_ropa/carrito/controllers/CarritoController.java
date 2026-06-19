package com.grupo3.tienda_ropa.carrito.controllers;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping
    public ResponseEntity<CarritoEntity> crearOObtenerCarrito(
            Principal principal) {

        CarritoEntity carrito =
                carritoService.obtenerOCrearCarritoPorEmail(
                        principal.getName()
                );

        return ResponseEntity.ok(carrito);
    }

    @GetMapping
    public ResponseEntity<CarritoEntity> obtenerCarrito(
            Principal principal) {

        CarritoEntity carrito =
                carritoService.obtenerOCrearCarritoPorEmail(
                        principal.getName()
                );

        return ResponseEntity.ok(carrito);
    }
}