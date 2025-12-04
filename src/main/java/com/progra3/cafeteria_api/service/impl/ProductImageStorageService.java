package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.service.port.IProductImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ProductImageStorageService implements IProductImageStorageService {

    private final Path productsDir;

    public ProductImageStorageService(@Value("${app.upload-dir}") String rootDir) {
        Path rootPath = Paths.get(rootDir).toAbsolutePath().normalize();
        this.productsDir = rootPath.resolve("products");
        try {
            Files.createDirectories(this.productsDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create products upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }

        String filename = java.util.UUID.randomUUID() + extension;
        Path destination = productsDir.resolve(filename);

        try {
            Files.copy(file.getInputStream(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store product image", e);
        }

        return "/uploads/products/" + filename;
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String prefix = "/uploads/";
        if (!imageUrl.startsWith(prefix)) {
            return; // Unexpected format, do nothing
        }

        // imageUrl = "/uploads/products/uuid.ext" → "products/uuid.ext"
        String relativePath = imageUrl.substring(prefix.length());

        try {
            Path rootPath = productsDir.getParent(); // .../uploads
            Path filePath = rootPath.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Podés loguearlo; no conviene romper la operación de negocio por esto
        }
    }
}
