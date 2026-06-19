package com.grupo3.tienda_ropa.carrito.repository;

import com.grupo3.tienda_ropa.carrito.entitys.CarritoEntity;
import com.grupo3.tienda_ropa.carrito.entitys.CarritoItem;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoItemRepo
        extends JpaRepository<CarritoItem, Long>{

    List<CarritoItem> findByCarrito(CarritoEntity carrito);

    List<CarritoItem> findByCarritoId(Long carritoId);

    Optional<CarritoItem> findByCarritoAndProducto(
            CarritoEntity carrito,
            Producto producto
    );

    Optional<CarritoItem> findByCarritoIdAndProductoId(
            Long carritoId,
            Long productoId
    );

    long countByCarritoId(Long carritoId);

    boolean existsByCarritoIdAndProductoId(
            Long carritoId,
            Long productoId
    );

    void deleteByCarrito(CarritoEntity carrito);

    void deleteByCarritoId(Long carritoId);

    void deleteByCarritoIdAndProductoId(
            Long carritoId,
            Long productoId
    );
}