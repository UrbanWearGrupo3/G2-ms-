package com.grupo3.tienda_ropa.usuario.controller;

import com.grupo3.tienda_ropa.usuario.deto.LoginRequest;
import com.grupo3.tienda_ropa.usuario.deto.LoginResponse;
import com.grupo3.tienda_ropa.usuario.deto.RegistroRequest;
import com.grupo3.tienda_ropa.usuario.deto.UsuarioResponse;
import com.grupo3.tienda_ropa.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse response = usuarioService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = usuarioService.login(request);
        return ResponseEntity.ok(response);
    }
}
