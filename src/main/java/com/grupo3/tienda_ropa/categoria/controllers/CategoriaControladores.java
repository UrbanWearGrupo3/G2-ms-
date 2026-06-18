package com.grupo3.tienda_ropa.categoria.controllers;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.grupo3.tienda_ropa.categoria.entity.CatergoriaEntity;
import com.grupo3.tienda_ropa.categoria.service.CategoriaService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/categorias")
public class CategoriaControladores {

    private final CategoriaService service;

    public CategoriaControladores(CategoriaService service) {
        this.service = service;
    }

    //subir productos
    @PostMapping
    public ResponseEntity<CatergoriaEntity> guardar(@Valid @RequestBody CatergoriaEntity categoria) {
        CatergoriaEntity saveCategory = service.saveCategory(categoria);
        return ResponseEntity.ok(saveCategory);
    }
    //mostrar todos los productos
    @GetMapping
    public List<CatergoriaEntity> getAllCategorys() {
        return service.getAllCategory();
    }
  
}
