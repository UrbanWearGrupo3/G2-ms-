package com.grupo3.tienda_ropa.Pedidos.service;

import com.grupo3.tienda_ropa.Pedidos.deto.PedidoMapper;
import com.grupo3.tienda_ropa.Pedidos.deto.PedidoResponse;
import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidosRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoService(
            PedidosRepository pedidoRepository,
            UsuarioRepository usuarioRepository,
            PedidoMapper pedidoMapper
    ) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoMapper = pedidoMapper;
    }

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public List<PedidoResponse> obtenerMisPedidos(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return pedidoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(pedidoMapper::toResponse)
                .toList();
    }

    public List<Pedido> obtenerMisPedidosPorEstado(
            String email,
            String estado
    ) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return pedidoRepository.findByUsuarioIdAndEstado(
                usuario.getId(),
                estado
        );
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidoRepository.findByEstado(estado);
    }
}