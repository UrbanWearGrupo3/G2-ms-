package com.grupo3.tienda_ropa.carrito.service;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.carrito.repository.CarritoItemRepo;
import com.grupo3.tienda_ropa.carrito.repository.CarritoRepository;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoItemService {

    private final CarritoItemRepo carritoItemRepo;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

   
    public CarritoItem agregarProducto(
            Long carritoId,
            Long productoId,
            Integer cantidad
    ) {

        CarritoEntity carrito = carritoRepository.findById(carritoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Carrito no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Producto no encontrado"));

        return carritoItemRepo
                .findByCarritoAndProducto(carrito, producto)
                .map(item -> {

                    item.setCantidad(
                            item.getCantidad() + cantidad
                    );

                    return carritoItemRepo.save(item);
                })
                .orElseGet(() -> {

                    CarritoItem item = new CarritoItem();

                    item.setCarrito(carrito);
                    item.setProducto(producto);
                    item.setCantidad(cantidad);

                    return carritoItemRepo.save(item);
                });
    }

    @Transactional(readOnly = true)
    public List<CarritoItem> obtenerItems(Long carritoId) {

        return carritoItemRepo.findByCarritoId(carritoId);
    }

    @Transactional(readOnly = true)
    public CarritoItem obtenerItem(
            Long carritoId,
            Long productoId
    ) {

        return carritoItemRepo
                .findByCarritoIdAndProductoId(
                        carritoId,
                        productoId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Producto no encontrado en el carrito"
                        ));
    }

    public CarritoItem aumentarCantidad(
            Long carritoId,
            Long productoId
    ) {

        CarritoItem item = obtenerItem(
                carritoId,
                productoId
        );

        item.setCantidad(
                item.getCantidad() + 1
        );

        return carritoItemRepo.save(item);
    }
    public void disminuirCantidad(
            Long carritoId,
            Long productoId
    ) {

        CarritoItem item = obtenerItem(
                carritoId,
                productoId
        );

        if (item.getCantidad() <= 1) {

            carritoItemRepo.delete(item);
            return;
        }

        item.setCantidad(
                item.getCantidad() - 1
        );

        carritoItemRepo.save(item);
    }

    public CarritoItem actualizarCantidad(
            Long carritoId,
            Long productoId,
            Integer cantidad
    ) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor a cero"
            );
        }

        CarritoItem item = obtenerItem(
                carritoId,
                productoId
        );

        item.setCantidad(cantidad);

        return carritoItemRepo.save(item);
    }

    public void eliminarProducto(
            Long carritoId,
            Long productoId
    ) {

        if (!carritoItemRepo.existsByCarritoIdAndProductoId(
                carritoId,
                productoId
        )) {

            throw new IllegalArgumentException(
                    "Producto no encontrado en el carrito"
            );
        }

        carritoItemRepo.deleteByCarritoIdAndProductoId(
                carritoId,
                productoId
        );
    }

    /**
     * Vaciar carrito completo.
     */
    public void vaciarCarrito(Long carritoId) {

        carritoItemRepo.deleteByCarritoId(carritoId);
    }

    /**
     * Cantidad total de registros del carrito.
     */
    @Transactional(readOnly = true)
    public long contarProductos(Long carritoId) {

        return carritoItemRepo.countByCarritoId(carritoId);
    }

    /**
     * Verifica si un producto ya existe.
     */
    @Transactional(readOnly = true)
    public boolean existeProducto(
            Long carritoId,
            Long productoId
    ) {

        return carritoItemRepo
                .existsByCarritoIdAndProductoId(
                        carritoId,
                        productoId
                );
    }
}