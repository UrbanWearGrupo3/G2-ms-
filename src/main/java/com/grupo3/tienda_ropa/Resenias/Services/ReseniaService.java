package com.grupo3.tienda_ropa.Resenias.Services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Resenias.Entity.ReseniasEntity;
import com.grupo3.tienda_ropa.Resenias.Repository.ReseniaRepository;

import com.grupo3.tienda_ropa.Resenias.dtos.CrearReseniaDTO;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;

@Service
public class ReseniaService {

    private final ReseniaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReseniaService(
            ReseniaRepository resenaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.resenaRepository = resenaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public ReseniasEntity crear(CrearReseniaDTO dto) {

        Producto producto = productoRepository
                .findById(dto.getProductoId())
                .orElseThrow(() ->
                        new RuntimeException("Producto no encontrado"));

        Usuario usuario = usuarioRepository
                .findById(dto.getUsuarioId())
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        boolean existe = resenaRepository
                .findByUsuarioIdAndProductoId(
                        dto.getUsuarioId(),
                        dto.getProductoId()
                )
                .isPresent();

        if (existe) {
            throw new RuntimeException(
                    "Ya realizaste una reseña para este producto"
            );
        }

        ReseniasEntity resena = new ReseniasEntity();

        resena.setComentario(dto.getComentario());
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setFecha(LocalDateTime.now());
        resena.setProducto(producto);
        resena.setUsuario(usuario);

        return resenaRepository.save(resena);
    }
}