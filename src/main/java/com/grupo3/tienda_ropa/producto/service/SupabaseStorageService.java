package com.grupo3.tienda_ropa.producto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final RestClient restClient;
    private final String bucketName;
    private final String supabaseUrl;

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceKey,
            @Value("${supabase.bucket-name}") String bucketName) {
        
        this.supabaseUrl = supabaseUrl;
        this.bucketName = bucketName;
        
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl + "/storage/v1")
                .defaultHeader("Authorization", "Bearer " + serviceKey)
                .defaultHeader("apikey", serviceKey)
                .build();
    }

    /**
     * Sube un archivo a Supabase Storage y retorna su URL pública.
     */
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no puede estar vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten formatos JPEG, PNG y WEBP");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            byte[] bytes = file.getBytes();
            
            restClient.post()
                    .uri("/object/{bucket}/{path}", bucketName, uniqueFilename)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();

            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, uniqueFilename);
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al leer los bytes de la imagen", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al comunicarse con Supabase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina un archivo de Supabase Storage mediante su URL pública.
     */
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        String prefix = "/object/public/" + bucketName + "/";
        int index = imageUrl.indexOf(prefix);
        if (index == -1) {
            return; 
        }

        String filename = imageUrl.substring(index + prefix.length());

        try {
            restClient.delete()
                    .uri("/object/{bucket}/{path}", bucketName, filename)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo eliminar la imagen física de Supabase: " + e.getMessage());
        }
    }
}
