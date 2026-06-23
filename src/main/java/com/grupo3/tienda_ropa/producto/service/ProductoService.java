package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.color.dto.ColorResponseDto;
import com.grupo3.tienda_ropa.color.entity.Color;
import com.grupo3.tienda_ropa.color.repository.ColorRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final VarianteRepository varianteRepository;
    private final CategoriaRepository categoriaRepository;
    private final SupabaseStorageService storageService;
    private final ColorRepository colorRepository;

    public ProductoService(ProductoRepository productoRepository,
            VarianteRepository varianteRepository,
            CategoriaRepository categoriaRepository,
            SupabaseStorageService storageService,
            ColorRepository colorRepository) {
        this.productoRepository = productoRepository;
        this.varianteRepository = varianteRepository;
        this.categoriaRepository = categoriaRepository;
        this.storageService = storageService;
        this.colorRepository = colorRepository;
    }

    // --- PRODUCT LOGIC ---
    // --- GUARDAR PRODUCTOS---
    public ProductoResponse save(ProductoRequest request) {
        if (request.getVariantes() != null) {
            long uniqueCount = request.getVariantes().stream()
                    .map(v -> v.getTalle().trim().toLowerCase() + "-" + v.getColorId())
                    .distinct()
                    .count();
            if (uniqueCount < request.getVariantes().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La lista de variantes contiene combinaciones de talle y color duplicadas.");
            }
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);
        producto.setActivo(true);



        if (request.getVariantes() != null) {
            for (VarianteRequest vr : request.getVariantes()) {
                Variante variante = new Variante();
                variante.setTalle(vr.getTalle());
                variante.setStock(vr.getStock());
                variante.setProducto(producto);

                // Resolver color por ID
                Color color = colorRepository.findById(vr.getColorId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Color no encontrado con id: " + vr.getColorId()));
                variante.setColor(color);

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

    // --- UPDATE PRODUCTOS ---
    public ProductoResponse update(Long id, ProductoRequest request) {
        if (request.getVariantes() != null) {
            long uniqueCount = request.getVariantes().stream()
                    .map(v -> v.getTalle().trim().toLowerCase() + "-" + v.getColorId())
                    .distinct()
                    .count();
            if (uniqueCount < request.getVariantes().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La lista de variantes contiene combinaciones de talle y color duplicadas.");
            }
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con id: " + id));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + request.getCategoriaId()));

        // Limpiar la imagen anterior en Supabase Storage únicamente cuando se reemplace
        // por una nueva
        String oldImageUrl = producto.getImagenUrl();
        if (oldImageUrl != null && !oldImageUrl.equals(request.getImagenUrl())) {
            storageService.deleteImageByUrl(oldImageUrl);
        }

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);

        // Synchronize variants
        if (request.getVariantes() != null) {
            Map<String, Variante> existingVariants = producto.getVariantes().stream()
                    .collect(Collectors.toMap(
                            v -> (v.getTalle().toLowerCase() + "-" + v.getColor().getId()),
                            v -> v,
                            (v1, v2) -> v1 // Handle any duplicates gracefully
                    ));

            List<Variante> updatedVariants = new ArrayList<>();

            for (VarianteRequest vr : request.getVariantes()) {
                String key = vr.getTalle().toLowerCase() + "-" + vr.getColorId();

                if (existingVariants.containsKey(key)) {
                    Variante existing = existingVariants.get(key);
                    existing.setStock(vr.getStock());
                    updatedVariants.add(existing);
                } else {
                    Variante nueva = new Variante();
                    nueva.setTalle(vr.getTalle());
                    nueva.setStock(vr.getStock());
                    nueva.setProducto(producto);

                    Color color = colorRepository.findById(vr.getColorId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Color no encontrado con id: " + vr.getColorId()));
                    nueva.setColor(color);

                    String barcode = vr.getCodigoBarras();
                    if (barcode == null || barcode.trim().isEmpty()) {
                        barcode = generateUniqueBarcode();
                    } else {
                        validateUniqueBarcode(barcode);
                    }
                    nueva.setCodigoBarras(barcode);
                    updatedVariants.add(nueva);
                }
            }

            producto.getVariantes().clear();
            producto.getVariantes().addAll(updatedVariants);
        }

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

        // Resolver color por ID
        Color color = colorRepository.findById(request.getColorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Color no encontrado con id: " + request.getColorId()));

        boolean exists = varianteRepository.existsByProductoIdAndTalleIgnoreCaseAndColorId(
                productoId, request.getTalle(), request.getColorId());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe una variante con talle: " + request.getTalle() + " y color: " + color.getNombre()
                            + " para este producto.");
        }

        Variante variante = new Variante();
        variante.setTalle(request.getTalle());
        variante.setColor(color);
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
                vr.setStock(v.getStock());
                vr.setCodigoBarras(v.getCodigoBarras());

                // Mapear color
                if (v.getColor() != null) {
                    vr.setColor(ColorResponseDto.builder()
                            .id(v.getColor().getId())
                            .nombre(v.getColor().getNombre())
                            .codigoHex(v.getColor().getCodigoHex())
                            .activo(v.getColor().getActivo())
                            .build());
                }

                return vr;
            }).collect(Collectors.toList());
            res.setVariantes(varList);
        }

        return res;
    }

}
