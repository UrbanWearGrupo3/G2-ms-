package com.grupo3.tienda_ropa.Pedidos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.Pedidos.entity.Pedido;
import com.grupo3.tienda_ropa.Pedidos.entity.PedidosDetalles;
import com.grupo3.tienda_ropa.Pedidos.repository.DetallePedidosRepository;
import com.grupo3.tienda_ropa.Pedidos.repository.PedidosRepository;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import com.grupo3.tienda_ropa.notification.model.NotificationType;
import com.grupo3.tienda_ropa.notification.service.EmailNotificationService;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepo carritoItemRepo;
    private final PedidosRepository pedidosRepository;
    private final DetallePedidosRepository detallePedidosRepository;
    private final EmailNotificationService emailNotificationService;
    private final UsuarioRepository usuarioRepository;

    // ==================== MÉTODOS AUXILIARES PARA USUARIO ====================

    /**
     * Obtiene un usuario por su email
     */
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    /**
     * Obtiene el ID de un usuario por su email
     */
    public Long obtenerUsuarioIdPorEmail(String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        return usuario.getId();
    }

    // ==================== MÉTODOS DE CONFIRMACIÓN DE PEDIDO ====================

    public Pedido confirmarPedidoPorEmail(String email) {
        Long usuarioId = obtenerUsuarioIdPorEmail(email);
        
        return confirmarPedido(usuarioId);
    }
    public Pedido confirmarPedido(Long usuarioId) {
        // 1. Obtener carrito del usuario
        CarritoEntity carrito = carritoRepository
                .findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // 2. Obtener items del carrito
        List<CarritoItem> items = carritoItemRepo.findByCarritoId(carrito.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 3. Validar cantidades
        items.forEach(item -> {
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new RuntimeException("Cantidad inválida para producto: " + 
                    item.getProducto().getNombre());
            }
        });

        // 4. Calcular total
        BigDecimal total = items.stream()
                .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Crear pedido
        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario());
        pedido.setEstado("PENDIENTE");
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(total);

        Pedido pedidoGuardado = pedidosRepository.save(pedido);

        // 6. Crear detalles del pedido
        List<PedidosDetalles> detalles = items.stream()
                .map(item -> {
                    PedidosDetalles detalle = new PedidosDetalles();
                    detalle.setPedido(pedidoGuardado);
                    detalle.setProducto(item.getProducto());
                    detalle.setCantidad(item.getCantidad());
                    return detalle;
                })
                .toList();

        detallePedidosRepository.saveAll(detalles);

        // 7. Limpiar carrito
        carritoItemRepo.deleteAll(items);

        // 8. Asociar detalles al pedido
        pedidoGuardado.setDetalles(detalles);

        // 9. Enviar notificación
        enviarNotificacionConfirmacion(pedidoGuardado);

        return pedidoGuardado;
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    public List<Pedido> obtenerPedidosUsuario(Long usuarioId) {
        return pedidosRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidosRepository.findByEstado(estado);
    }

    public List<Pedido> obtenerPedidosUsuarioPorEstado(Long usuarioId, String estado) {
        return pedidosRepository.findByUsuarioIdAndEstado(usuarioId, estado);
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidosRepository.findAll();
    }

    public Pedido obtenerPedidoPorId(Long pedidoId) {
        return pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    // ==================== MÉTODOS DE ACTUALIZACIÓN ====================

    public Pedido actualizarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstado(nuevoEstado);

        Pedido pedidoGuardado = pedidosRepository.save(pedido);

        enviarNotificacionEstado(pedidoGuardado, nuevoEstado);

        return pedidoGuardado;
    }

    // ==================== MÉTODOS DE NOTIFICACIÓN ====================

    private void enviarNotificacionConfirmacion(Pedido pedido) {
        try {
            List<Map<String, Object>> emailItems = pedido.getDetalles().stream()
                    .map(d -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("productoNombre", d.getProducto().getNombre());
                        itemMap.put("talle", "N/A");
                        itemMap.put("cantidad", d.getCantidad());
                        itemMap.put("precio", d.getProducto().getPrecio().multiply(BigDecimal.valueOf(d.getCantidad())).toString());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("clientName", pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
            templateData.put("orderNumber", pedido.getId().toString());
            templateData.put("total", pedido.getTotal().toString());
            templateData.put("items", emailItems);

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .recipient(pedido.getUsuario().getEmail())
                    .subject("Confirmación de Pedido #" + pedido.getId() + " - UrbanWear")
                    .type(NotificationType.ORDER_CONFIRMATION)
                    .templateData(templateData)
                    .build();

            emailNotificationService.sendEmail(notificationRequest);
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de confirmación para pedido " + pedido.getId() + ": " + e.getMessage());
        }
    }

    private void enviarNotificacionEstado(Pedido pedido, String nuevoEstado) {
        try {
            List<Map<String, Object>> emailItems = pedido.getDetalles().stream()
                    .map(d -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("productoNombre", d.getProducto().getNombre());
                        itemMap.put("cantidad", d.getCantidad());
                        itemMap.put("precio", d.getProducto().getPrecio().multiply(BigDecimal.valueOf(d.getCantidad())).toString());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            String mensajeEstado;
            switch (nuevoEstado) {
                case "APROBADO":
                    mensajeEstado = "¡Buenas noticias! Tu pago ha sido aprobado. Estamos preparando tus artículos.";
                    break;
                case "RECHAZADO":
                    mensajeEstado = "Lo sentimos, el pago ha sido rechazado. Por favor, intenta realizar el pago nuevamente.";
                    break;
                case "FALLIDO":
                    mensajeEstado = "El intento de pago ha fallado. Revisa tu medio de pago y vuelve a intentarlo.";
                    break;
                case "PENDIENTE_PAGO":
                    mensajeEstado = "Tu pago está pendiente de procesamiento. Te informaremos en cuanto se complete.";
                    break;
                default:
                    mensajeEstado = "Tu pedido ha cambiado al estado: " + nuevoEstado + ".";
                    break;
            }

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("clientName", pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido());
            templateData.put("orderNumber", pedido.getId().toString());
            templateData.put("newStatus", nuevoEstado);
            templateData.put("statusMessage", mensajeEstado);
            templateData.put("total", pedido.getTotal().toString());
            templateData.put("items", emailItems);

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .recipient(pedido.getUsuario().getEmail())
                    .subject("Actualización de tu Pedido #" + pedido.getId() + " - UrbanWear")
                    .type(NotificationType.ORDER_STATUS_UPDATE)
                    .templateData(templateData)
                    .build();

            emailNotificationService.sendEmail(notificationRequest);
        } catch (Exception e) {
            System.err.println("Error al enviar notificación de estado para pedido " + pedido.getId() + ": " + e.getMessage());
        }
    }
}