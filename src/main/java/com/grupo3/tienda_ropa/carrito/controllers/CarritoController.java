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

                String email = principal.getName();
                CarritoEntity carrito = carritoService.obtenerOCrearCarritoPorEmail(email);
                return ResponseEntity.ok(carrito);
        }

        @GetMapping
        public ResponseEntity<CarritoEntity> obtenerCarrito(
                        Principal principal) {

                String email = principal.getName();
                CarritoEntity carrito = carritoService.obtenerCarritoPorEmail(email);
                return ResponseEntity.ok(carrito);
        }
}