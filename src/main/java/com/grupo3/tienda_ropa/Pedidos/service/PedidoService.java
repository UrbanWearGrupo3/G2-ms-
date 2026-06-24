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
import com.grupo3.tienda_ropa.cupon.service.CuponService;
import com.grupo3.tienda_ropa.usuario.repository.UsuarioRepository;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import com.grupo3.tienda_ropa.usuario.entity.Usuario;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.Pedidos.deto.CompraDirectaRequest;

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
    private final CuponService cuponService;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    // ==================== MÉTODO PARA OBTENER ID DE USUARIO POR EMAIL ====================
    
    public Long obtenerUsuarioIdPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        return usuario.getId();
    }

    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }

    // ==================== MÉTODOS DE CONFIRMACIÓN DE PEDIDO ====================

    public Pedido confirmarPedido(Long usuarioId) {
        return confirmarPedido(usuarioId, (String) null);
    }

    public Pedido confirmarPedido(Long usuarioId, String cuponCodigo) {
        CarritoEntity carrito = carritoRepository
                .findByUsuario_Id(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Carrito no encontrado"));

        List<CarritoItem> items =
                carritoItemRepo.findByCarritoId(carrito.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        BigDecimal subtotal = items.stream()
                .map(item -> item.getProducto().getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuento = BigDecimal.ZERO;
        String cuponAplicado = null;

        if (cuponCodigo != null && !cuponCodigo.trim().isEmpty()) {
            var validation = cuponService.validarYCalcularDescuento(cuponCodigo, usuarioId);
            if (!validation.getValido()) {
                throw new RuntimeException("Cupón inválido: " + validation.getMensajeError());
            }
            descuento = validation.getDescuentoAplicado();
            cuponAplicado = validation.getCodigo();
        }

        BigDecimal total = subtotal.subtract(descuento);

        Pedido pedido = new Pedido();

        pedido.setUsuario(carrito.getUsuario());
        pedido.setEstado("PAGADO"); // Compra fingida: salta estado PENDIENTE
        pedido.setFecha(LocalDateTime.now());
        // Si tu entidad NO tiene estos campos, coméntalos o elimínalos
        // pedido.setSubtotal(subtotal);  // Comentar si no existe
        // pedido.setDescuento(descuento); // Comentar si no existe
        // pedido.setCuponCodigo(cuponAplicado); // Comentar si no existe
        pedido.setTotal(total);

        Pedido pedidoGuardado = pedidosRepository.save(pedido);

        if (cuponAplicado != null) {
            cuponService.registrarUso(cuponAplicado);
        }

        List<PedidosDetalles> detalles = items.stream()
                .map(item -> {
                    // Descontar stock de la variante
                    Producto prod = item.getProducto();
                    if (prod.getVariantes() != null && !prod.getVariantes().isEmpty()) {
                        // Buscar la primera variante con stock suficiente, o usar la primera si no hay
                        Variante targetVariant = prod.getVariantes().stream()
                            .filter(v -> v.getStock() >= item.getCantidad())
                            .findFirst()
                            .orElse(prod.getVariantes().get(0));
                        targetVariant.setStock(Math.max(0, targetVariant.getStock() - item.getCantidad()));
                    }

                    PedidosDetalles detalle = new PedidosDetalles();
                    detalle.setPedido(pedidoGuardado);
                    detalle.setProducto(prod);
                    detalle.setCantidad(item.getCantidad());
                    return detalle;
                })
                .toList();

        detallePedidosRepository.saveAll(detalles);

        carritoItemRepo.deleteAll(items);

        pedidoGuardado.setDetalles(detalles);

        enviarNotificacionConfirmacion(pedidoGuardado);

        return pedidoGuardado;
    }

    public Pedido confirmarPedidoPorEmail(String email) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        return confirmarPedido(usuario.getId(), (String) null);
    }

    public Pedido confirmarPedidoPorEmail(String email, String cuponCodigo) {
        Usuario usuario = obtenerUsuarioPorEmail(email);
        return confirmarPedido(usuario.getId(), cuponCodigo);
    }

    // ==================== MÉTODO DE COMPRA DIRECTA ====================

    public Pedido comprarDirecto(Long usuarioId, CompraDirectaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (producto.getActivo() != null && !producto.getActivo()) {
            throw new RuntimeException("El producto no está activo");
        }

        BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(request.getCantidad()));
        BigDecimal descuento = BigDecimal.ZERO;
        String cuponAplicado = null;

        if (request.getCuponCodigo() != null && !request.getCuponCodigo().trim().isEmpty()) {
            var validation = cuponService.validarYCalcularDescuento(request.getCuponCodigo(), usuarioId, subtotal);
            if (!validation.getValido()) {
                throw new RuntimeException("Cupón inválido: " + validation.getMensajeError());
            }
            descuento = validation.getDescuentoAplicado();
            cuponAplicado = validation.getCodigo();
        }

        BigDecimal total = subtotal.subtract(descuento);

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado("PAGADO"); // Compra fingida: salta estado PENDIENTE
        pedido.setFecha(LocalDateTime.now());
        // Si tu entidad NO tiene estos campos, coméntalos o elimínalos
        // pedido.setSubtotal(subtotal);  // Comentar si no existe
        // pedido.setDescuento(descuento); // Comentar si no existe
        // pedido.setCuponCodigo(cuponAplicado); // Comentar si no existe
        pedido.setTotal(total);
        // pedido.setDireccionEnvio(request.getDireccionEnvio()); // Comentar si no existe

        Pedido pedidoGuardado = pedidosRepository.save(pedido);

        if (cuponAplicado != null) {
            cuponService.registrarUso(cuponAplicado);
        }

        // Descontar stock de la variante
        if (producto.getVariantes() != null && !producto.getVariantes().isEmpty()) {
            Variante targetVariant = producto.getVariantes().stream()
                .filter(v -> v.getStock() >= request.getCantidad())
                .findFirst()
                .orElse(producto.getVariantes().get(0));
            targetVariant.setStock(Math.max(0, targetVariant.getStock() - request.getCantidad()));
        }

        PedidosDetalles detalle = new PedidosDetalles();
        detalle.setPedido(pedidoGuardado);
        detalle.setProducto(producto);
        detalle.setCantidad(request.getCantidad());

        detallePedidosRepository.save(detalle);

        pedidoGuardado.setDetalles(List.of(detalle));

        enviarNotificacionConfirmacion(pedidoGuardado);

        return pedidoGuardado;
    }

    public Pedido comprarDirecto(Long usuarioId, Long productoId, Integer cantidad) {
        CompraDirectaRequest request = new CompraDirectaRequest();
        request.setProductoId(productoId);
        request.setCantidad(cantidad);
        return comprarDirecto(usuarioId, request);
    }

    // ==================== MÉTODOS DE CONSULTA ====================

    public List<Pedido> obtenerPedidosUsuario(Long usuarioId) {
        return pedidosRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> obtenerPedidosPorEstado(String estado) {
        return pedidosRepository.findByEstado(estado);
    }

    public List<Pedido> obtenerPedidosUsuarioPorEstado(
            Long usuarioId,
            String estado
    ) {
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
                case "PAGADO":
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