package com.grupo3.tienda_ropa.usuario.controller;

import com.grupo3.tienda_ropa.usuario.deto.UsuarioResponse;
import com.grupo3.tienda_ropa.usuario.deto.UsuarioUpdateRequest;
import com.grupo3.tienda_ropa.usuario.entity.Rol;
import com.grupo3.tienda_ropa.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> getProfile(Principal principal) {
        UsuarioResponse response = usuarioService.getProfile(principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse response = usuarioService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> findAll(Pageable pageable) {
        Page<UsuarioResponse> response = usuarioService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/rol")
    public ResponseEntity<UsuarioResponse> updateRol(
            @PathVariable Long id,
            @RequestParam Rol rol
    ) {
        UsuarioResponse response = usuarioService.updateRol(id, rol);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
