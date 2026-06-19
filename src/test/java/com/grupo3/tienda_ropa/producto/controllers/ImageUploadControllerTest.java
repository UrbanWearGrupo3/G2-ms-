package com.grupo3.tienda_ropa.producto.controllers;

import com.grupo3.tienda_ropa.producto.service.SupabaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageUploadControllerTest {

    @Mock
    private SupabaseStorageService storageService;

    @InjectMocks
    private ImageUploadController imageUploadController;

    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "test image content".getBytes()
        );
    }

    @Test
    void testUploadImage_Success() {
        String expectedUrl = "https://supabase.co/storage/v1/object/public/productos/uuid-test-image.png";
        when(storageService.uploadImage(mockFile)).thenReturn(expectedUrl);

        ResponseEntity<Map<String, String>> response = imageUploadController.uploadImage(mockFile);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedUrl, response.getBody().get("url"));
        verify(storageService, times(1)).uploadImage(mockFile);
    }
}
