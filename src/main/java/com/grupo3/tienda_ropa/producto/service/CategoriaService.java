package com.grupo3.tienda_ropa.producto.service;

import com.grupo3.tienda_ropa.producto.deto.CategoriaRequest;
import com.grupo3.tienda_ropa.producto.deto.CategoriaResponse;
import com.grupo3.tienda_ropa.producto.entity.Categoria;
import com.grupo3.tienda_ropa.producto.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setActivo(true);

        Categoria saved = categoriaRepository.save(categoria);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + id));
        return mapToResponse(categoria);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + id));

        // Validar que no exista otra categoría con el mismo nombre
        categoriaRepository.findByNombreIgnoreCase(request.getNombre())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Ya existe otra categoría con el nombre: " + request.getNombre());
                    }
                });

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria saved = categoriaRepository.save(categoria);
        return mapToResponse(saved);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + id));
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaResponse toggleActivo(Long id, Boolean activo) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada con id: " + id));
        categoria.setActivo(activo);
        Categoria saved = categoriaRepository.save(categoria);
        return mapToResponse(saved);
    }

    private CategoriaResponse mapToResponse(Categoria categoria) {
        CategoriaResponse response = new CategoriaResponse();
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setDescripcion(categoria.getDescripcion());
        response.setActivo(categoria.getActivo());
        return response;
    }
}
