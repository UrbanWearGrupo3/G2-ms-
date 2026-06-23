package com.grupo3.tienda_ropa.Resenias.Services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Resenias.Entity.ReseniasEntity;
import com.grupo3.tienda_ropa.Resenias.Repository.ReseniaRepository;
import com.grupo3.tienda_ropa.Resenias.dtos.ActualizarReseniaDTO;
import com.grupo3.tienda_ropa.Resenias.dtos.CrearReseniaDTO;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import com.grupo3.tienda_ropa.usuario.entity.Rol;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

@Service
public class ReseniaService {

    private final ReseniaRepository resenaRepository;
    private final ProductoRepository productoRepository;

    public ReseniaService(
            ReseniaRepository resenaRepository,
            ProductoRepository productoRepository) {
        this.resenaRepository = resenaRepository;
        this.productoRepository = productoRepository;
    }

    // Crear reseña — el usuario se obtiene del token JWT
    public ReseniasEntity crear(CrearReseniaDTO dto, Usuario usuario) {

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        boolean existe = resenaRepository
                .findByUsuarioIdAndProductoId(usuario.getId(), dto.getProductoId())
                .isPresent();

        if (existe) {
            throw new RuntimeException("Ya realizaste una reseña para este producto");
        }

        ReseniasEntity resena = new ReseniasEntity();
        resena.setComentario(dto.getComentario());
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setFecha(LocalDateTime.now());
        resena.setProducto(producto);
        resena.setUsuario(usuario);

        return resenaRepository.save(resena);
    }

    // Obtener reseñas por producto — público
    public List<ReseniasEntity> obtenerPorProducto(Long productoId) {

        if (!productoRepository.existsById(productoId)) {
            throw new RuntimeException("Producto no encontrado");
        }

        return resenaRepository.findByProductoId(productoId);
    }

    // Actualizar reseña — solo el autor autenticado puede modificar su propia
    // reseña
    public ReseniasEntity actualizar(Long id, ActualizarReseniaDTO dto, Usuario usuario) {

        ReseniasEntity resena = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        if (!resena.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tenés permiso para modificar esta reseña");
        }

        resena.setComentario(dto.getComentario());
        resena.setPuntuacion(dto.getPuntuacion());
        resena.setFecha(LocalDateTime.now());

        return resenaRepository.save(resena);
    }

    // Eliminar reseña — el autor o un administrador pueden eliminar
    public void eliminar(Long id, Usuario usuario) {

        ReseniasEntity resena = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        boolean esAutor = resena.getUsuario().getId().equals(usuario.getId());
        boolean esAdmin = usuario.getRol() == Rol.ADMIN;

        if (!esAutor && !esAdmin) {
            throw new RuntimeException("No tenés permiso para eliminar esta reseña");
        }

        resenaRepository.delete(resena);
    }
}

