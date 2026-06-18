package com.grupo3.tienda_ropa.categoria.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grupo3.tienda_ropa.categoria.entity.CatergoriaEntity;
import com.grupo3.tienda_ropa.categoria.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    public CatergoriaEntity saveCategory(CatergoriaEntity categoria) {
        return repository.save(categoria);
    }

    public List<CatergoriaEntity> getAllCategory() {
        return repository.findAll();
    }
    

}