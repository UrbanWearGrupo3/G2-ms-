package com.grupo3.tienda_ropa.carrito.service;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioService usuarioService;

    public CarritoEntity obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuario_Id(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId);
                    CarritoEntity carrito = new CarritoEntity();
                    carrito.setUsuario(usuario);
                    return carritoRepository.save(carrito);
                });
    }

    public CarritoEntity obtenerOCrearCarritoPorEmail(String email) {
        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email);
        return obtenerOCrearCarrito(usuario.getId());
    }

    public CarritoEntity obtenerCarrito(Long usuarioId) {
        return obtenerOCrearCarrito(usuarioId);
    }

    public CarritoEntity obtenerCarritoPorEmail(String email) {
        return obtenerOCrearCarritoPorEmail(email);
    }
}