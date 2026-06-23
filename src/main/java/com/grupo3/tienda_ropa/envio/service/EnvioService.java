package com.grupo3.tienda_ropa.envio.service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.envio.dto.CotizacionResultDto;
import com.grupo3.tienda_ropa.envio.dto.EnvioResultDto;
import com.grupo3.tienda_ropa.envio.event.ShipmentStatusChangedEvent;
import com.grupo3.tienda_ropa.envio.model.Envio;
import com.grupo3.tienda_ropa.envio.model.EnvioEstado;
import com.grupo3.tienda_ropa.envio.repository.EnvioRepository;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategy;
import com.grupo3.tienda_ropa.envio.strategy.EnvioStrategyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PedidosRepository pedidosRepository;
    private final EnvioStrategyContext strategyContext;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Obtiene una cotización de envío para un pedido con un proveedor y dirección específica.
     */
    @Transactional(readOnly = true)
    public CotizacionResultDto cotizarEnvio(Long pedidoId, String proveedor, String direccionDestino) {
        Pedido pedido = obtenerPedidoValido(pedidoId);
        EnvioStrategy strategy = strategyContext.getStrategy(proveedor);
        return strategy.cotizarEnvio(pedido, direccionDestino);
    }

    /**
     * Crea y registra un envío asociado a un pedido.
     */
    @Transactional
    public Envio crearEnvio(Long pedidoId, String proveedor, String direccionDestino) {
        // Verificar si ya existe un envío registrado para este pedido
        envioRepository.findByPedidoId(pedidoId).ifPresent(e -> {
            throw new IllegalStateException("Ya existe un envío registrado para el pedido ID: " + pedidoId);
        });

        Pedido pedido = obtenerPedidoValido(pedidoId);
        EnvioStrategy strategy = strategyContext.getStrategy(proveedor);

        // Llamar a la API externa a través del adaptador/estrategia
        EnvioResultDto resultDto = strategy.crearEnvio(pedido, direccionDestino);

        // Crear la entidad de envío y guardarla en la base de datos
        Envio envio = new Envio();
        envio.setPedido(pedido);
        envio.setProveedorEnvio(strategy.getProveedor());
        envio.setCodigoSeguimiento(resultDto.getCodigoSeguimiento());
        envio.setCosto(resultDto.getCosto());
        envio.setDireccionDestino(direccionDestino);
        envio.setEstado(EnvioEstado.PREPARANDO);

        Envio savedEnvio = envioRepository.save(envio);

        // Actualizar la dirección de envío en el pedido (si corresponde) y el estado
        pedido.setDireccionEnvio(direccionDestino);
        pedido.setEstado("DESPACHADO"); // O el estado de pedido adecuado
        pedidosRepository.save(pedido);

        // Publicar evento de creación de envío para enviar notificaciones por correo
        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(savedEnvio));

        return savedEnvio;
    }

    /**
     * Actualiza el estado de un envío y gatilla la notificación correspondiente.
     */
    @Transactional
    public Envio actualizarEstadoEnvio(Long envioId, EnvioEstado nuevoEstado) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + envioId));

        EnvioEstado estadoAnterior = envio.getEstado();
        if (estadoAnterior == nuevoEstado) {
            return envio;
        }

        envio.setEstado(nuevoEstado);
        envio.setFechaActualizacion(LocalDateTime.now());
        Envio updatedEnvio = envioRepository.save(envio);

        // Si el estado cambia a ENTREGADO, también podríamos actualizar el estado del pedido a ENTREGADO
        if (nuevoEstado == EnvioEstado.ENTREGADO) {
            Pedido pedido = envio.getPedido();
            if (pedido != null) {
                pedido.setEstado("ENTREGADO");
                pedidosRepository.save(pedido);
            }
        }

        // Publicar evento para disparar notificaciones
        eventPublisher.publishEvent(new ShipmentStatusChangedEvent(updatedEnvio));

        return updatedEnvio;
    }

    /**
     * Consulta el tracking actual directamente desde el proveedor (a través del adaptador).
     */
    @Transactional(readOnly = true)
    public String consultarTrackingDesdeProveedor(Long envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + envioId));

        if (envio.getCodigoSeguimiento() == null) {
            return "El envío no cuenta con un código de seguimiento aún.";
        }

        EnvioStrategy strategy = strategyContext.getStrategy(envio.getProveedorEnvio());
        return strategy.consultarSeguimiento(envio.getCodigoSeguimiento(), envio.getEstado());
    }

    /**
     * Obtiene los proveedores de envío soportados por el sistema.
     */
    public List<String> obtenerProveedoresSoportados() {
        return strategyContext.getProveedoresSoportados();
    }

    /**
     * Obtiene los detalles de un envío por su ID.
     */
    @Transactional(readOnly = true)
    public Envio obtenerEnvioPorId(Long envioId) {
        return envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + envioId));
    }

    /**
     * Obtiene los detalles de un envío por el ID del pedido.
     */
    @Transactional(readOnly = true)
    public Envio obtenerEnvioPorPedidoId(Long pedidoId) {
        return envioRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No se encontró envío para el pedido ID: " + pedidoId));
    }

    private Pedido obtenerPedidoValido(Long pedidoId) {
        return pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));
    }

    public void validarAccesoPedido(Long pedidoId, Usuario usuarioLogueado, boolean isAdminOrSuper) {
        if (isAdminOrSuper) {
            return;
        }
        Pedido pedido = obtenerPedidoValido(pedidoId);
        if (pedido.getUsuario() == null || !pedido.getUsuario().getId().equals(usuarioLogueado.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para acceder a este pedido");
        }
    }

    public void validarAccesoEnvio(Long envioId, Usuario usuarioLogueado, boolean isAdminOrSuper) {
        if (isAdminOrSuper) {
            return;
        }
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + envioId));
        if (envio.getPedido() == null || envio.getPedido().getUsuario() == null ||
                !envio.getPedido().getUsuario().getId().equals(usuarioLogueado.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para acceder a este envío");
        }
    }
}
