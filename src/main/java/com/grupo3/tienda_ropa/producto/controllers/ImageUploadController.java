package com.grupo3.tienda_ropa.producto.controllers;

import com.grupo3.tienda_ropa.producto.service.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/productos/upload")
public class ImageUploadController {

    private final SupabaseStorageService storageService;

    public ImageUploadController(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        String publicUrl = storageService.uploadImage(file);
        return ResponseEntity.ok(Map.of("url", publicUrl));
    }
}
