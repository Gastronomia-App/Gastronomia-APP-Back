package com.progra3.cafeteria_api.controller;


import com.progra3.cafeteria_api.service.port.IProductImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final IProductImageStorageService productImageStorageService;
    // Simple DTO for the response
    public record FileUploadResponse(String url) {}

    @PostMapping("/products")
    public ResponseEntity<FileUploadResponse> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = productImageStorageService.store(file);
        return ResponseEntity.ok(new FileUploadResponse(url));
    }
}