package com.grupo3.tienda_ropa.Resenias.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.Resenias.Entity.ReseniasEntity;
import com.grupo3.tienda_ropa.Resenias.Services.ReseniaService;
import com.grupo3.tienda_ropa.Resenias.dtos.CrearReseniaDTO;
import com.grupo3.tienda_ropa.Resenias.dtos.ActualizarReseniaDTO;
import com.grupo3.tienda_ropa.Resenias.dtos.ReseniaResponseDTO;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

@RestController
@RequestMapping("/api/resenias")
public class ReseniasControllers {

    private final ReseniaService reseniaService;

    public ReseniasControllers(ReseniaService reseniaService) {
        this.reseniaService = reseniaService;
    }

    // Crear reseña — solo usuarios autenticados
    @PostMapping
    public ResponseEntity<ReseniaResponseDTO> crear(
            @RequestBody CrearReseniaDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        ReseniasEntity resenia = reseniaService.crear(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(resenia));
    }

    // Obtener reseñas por producto — público
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ReseniaResponseDTO>> obtenerPorProducto(@PathVariable Long productoId) {
        List<ReseniaResponseDTO> resenias = reseniaService.obtenerPorProducto(productoId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resenias);
    }

    // Actualizar reseña — solo el autor autenticado
    @PutMapping("/{id}")
    public ResponseEntity<ReseniaResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ActualizarReseniaDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        ReseniasEntity resenia = reseniaService.actualizar(id, dto, usuario);
        return ResponseEntity.ok(toResponseDTO(resenia));
    }

    // Eliminar reseña — solo el autor autenticado o ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {

        reseniaService.eliminar(id, usuario);
        return ResponseEntity.noContent().build();
    }

    // Mapeo de entidad a DTO de respuesta
    private ReseniaResponseDTO toResponseDTO(ReseniasEntity entity) {
        ReseniaResponseDTO dto = new ReseniaResponseDTO();
        dto.setId(entity.getId());
        dto.setUsuarioId(entity.getUsuario().getId());
        dto.setNombreUsuario(entity.getUsuario().getNombre());
        dto.setProductoId(entity.getProducto().getId());
        dto.setComentario(entity.getComentario());
        dto.setPuntuacion(entity.getPuntuacion());
        dto.setFecha(entity.getFecha());
        return dto;
    }
}
