package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.producto.deto.*;
import com.grupo3.tienda_ropa.producto.entity.Categoria;
import com.grupo3.tienda_ropa.producto.entity.Producto;
import com.grupo3.tienda_ropa.producto.entity.Variante;
import com.grupo3.tienda_ropa.producto.repository.CategoriaRepository;
import com.grupo3.tienda_ropa.producto.repository.ProductoRepository;
import com.grupo3.tienda_ropa.producto.repository.VarianteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VarianteRepository varianteRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           VarianteRepository varianteRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // --- PRODUCT LOGIC ---
    // --- GUARDAR PRODUCTOS---
    public ProductoResponse save(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setMarca(request.getMarca());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);
        producto.setActivo(true);

        if (request.getVariantes() != null) {
            for (VarianteRequest vr : request.getVariantes()) {
                Variante variante = new Variante();
                variante.setTalle(vr.getTalle());
                variante.setColor(vr.getColor());
                variante.setStock(vr.getStock());
                variante.setProducto(producto);

                String barcode = vr.getCodigoBarras();
                if (barcode == null || barcode.trim().isEmpty()) {
                    barcode = generateUniqueBarcode();
                } else {
                    validateUniqueBarcode(barcode);
                }
                variante.setCodigoBarras(barcode);
                producto.getVariantes().add(variante);
            }
        }

        Producto saved = productoRepository.save(producto);
        return mapToProductoResponse(saved);
    }
        //--- UPDATE PRODUCTOS ---
    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Producto no encontrado con id: " + id));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setMarca(request.getMarca());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);

        Producto saved = productoRepository.save(producto);
        return mapToProductoResponse(saved);
    }
    /// --- GET FIND BY ID---
    @Transactional(readOnly = true)
    public ProductoResponse findById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Producto no encontrado con id: " + id));
        return mapToProductoResponse(producto);
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponse> findAll(Long categoriaId, String talle, String color, 
                                          BigDecimal precioMin, BigDecimal precioMax, 
                                          String nombre, Boolean activo, Pageable pageable) {
        Boolean activeFilter = (activo != null) ? activo : true;
        Page<Producto> page = productoRepository.findByFiltros(categoriaId, talle, color, 
                precioMin, precioMax, nombre, activeFilter, pageable);
        return page.map(this::mapToProductoResponse);
    }
    

    public void deleteById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Producto no encontrado con id: " + id));
        producto.setActivo(false); // Baja lógica
        productoRepository.save(producto);
    }

    public ProductoResponse toggleActivo(Long id, Boolean activo) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Producto no encontrado con id: " + id));
        producto.setActivo(activo);
        Producto saved = productoRepository.save(producto);
        return mapToProductoResponse(saved);
    }

    // --- VARIANTS LOGIC ---

    public ProductoResponse addVariante(Long productoId, VarianteRequest request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Producto no encontrado con id: " + productoId));

        boolean exists = varianteRepository.existsByProductoIdAndTalleIgnoreCaseAndColorIgnoreCase(
                productoId, request.getTalle(), request.getColor());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Ya existe una variante con talle: " + request.getTalle() + " y color: " + request.getColor() + " para este producto.");
        }

        Variante variante = new Variante();
        variante.setTalle(request.getTalle());
        variante.setColor(request.getColor());
        variante.setStock(request.getStock());
        variante.setProducto(producto);

        String barcode = request.getCodigoBarras();
        if (barcode == null || barcode.trim().isEmpty()) {
            barcode = generateUniqueBarcode();
        } else {
            validateUniqueBarcode(barcode);
        }
        variante.setCodigoBarras(barcode);

        producto.getVariantes().add(variante);
        productoRepository.save(producto);

        return mapToProductoResponse(producto);
    }


    public ProductoResponse updateStock(Long varianteId, Integer nuevoStock) {
        Variante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Variante no encontrada con id: " + varianteId));

        if (nuevoStock < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El stock no puede ser negativo");
        }

        variante.setStock(nuevoStock);
        varianteRepository.save(variante);

        return mapToProductoResponse(variante.getProducto());
    }

    // --- HELPERS & MAPPINGS ---

    private String generateUniqueBarcode() {
        String barcode;
        do {
            barcode = "VAR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (varianteRepository.findByCodigoBarras(barcode).isPresent());
        return barcode;
    }

    private void validateUniqueBarcode(String barcode) {
        if (varianteRepository.findByCodigoBarras(barcode).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "El código de barras ya se encuentra registrado: " + barcode);
        }
    }

    private ProductoResponse mapToProductoResponse(Producto producto) {
        ProductoResponse res = new ProductoResponse();
        res.setId(producto.getId());
        res.setNombre(producto.getNombre());
        res.setDescripcion(producto.getDescripcion());
        res.setPrecio(producto.getPrecio());
        res.setMarca(producto.getMarca());
        res.setImagenUrl(producto.getImagenUrl());
        res.setActivo(producto.getActivo());
        res.setFechaCreacion(producto.getFechaCreacion());
        res.setFechaActualizacion(producto.getFechaActualizacion());

        if (producto.getCategoria() != null) {
            CategoriaResponse catRes = new CategoriaResponse();
            catRes.setId(producto.getCategoria().getId());
            catRes.setNombre(producto.getCategoria().getNombre());
            catRes.setDescripcion(producto.getCategoria().getDescripcion());
            catRes.setActivo(producto.getCategoria().getActivo());
            res.setCategoria(catRes);
        }

        if (producto.getVariantes() != null) {
            List<VarianteResponse> varList = producto.getVariantes().stream().map(v -> {
                VarianteResponse vr = new VarianteResponse();
                vr.setId(v.getId());
                vr.setTalle(v.getTalle());
                vr.setColor(v.getColor());
                vr.setStock(v.getStock());
                vr.setCodigoBarras(v.getCodigoBarras());
                return vr;
            }).collect(Collectors.toList());
            res.setVariantes(varList);
        }

        return res;
    }

}
