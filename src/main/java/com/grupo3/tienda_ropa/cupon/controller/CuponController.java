package com.grupo3.tienda_ropa.cupon.controller;

import com.grupo3.tienda_ropa.cupon.dto.CuponDescuentoDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponRequestDto;
import com.grupo3.tienda_ropa.cupon.dto.CuponResponseDto;
import com.grupo3.tienda_ropa.cupon.dto.ValidarCuponRequestDto;
import com.grupo3.tienda_ropa.cupon.service.CuponService;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class CuponController {

    private final CuponService cuponService;

    @PostMapping
    public ResponseEntity<CuponResponseDto> crearCupon(@Valid @RequestBody CuponRequestDto dto) {
        CuponResponseDto response = cuponService.crearCupon(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CuponResponseDto>> obtenerTodosLosCupones() {
        List<CuponResponseDto> response = cuponService.obtenerTodosLosCupones();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuponResponseDto> obtenerCuponPorId(@PathVariable Long id) {
        CuponResponseDto response = cuponService.obtenerCuponPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuponResponseDto> actualizarCupon(
            @PathVariable Long id,
            @Valid @RequestBody CuponRequestDto dto) {
        CuponResponseDto response = cuponService.actualizarCupon(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCupon(@PathVariable Long id) {
        cuponService.eliminarCupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validar")
    public ResponseEntity<CuponDescuentoDto> validarCupon(@Valid @RequestBody ValidarCuponRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Long usuarioId = usuario.getId();

        CuponDescuentoDto response = cuponService.validarYCalcularDescuento(dto.getCodigo(), usuarioId);
        return ResponseEntity.ok(response);
    }
}
