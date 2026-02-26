package com.progra3.cafeteria_api.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.progra3.cafeteria_api.service.port.IProductImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ProductImageStorageService implements IProductImageStorageService {

    private final Cloudinary cloudinary;

    public ProductImageStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "products")
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload image to Cloudinary", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // No interrumpimos la operacion de negocio si falla el borrado en Cloudinary
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary.
     * Formato: https://res.cloudinary.com/{cloud}/image/upload/v{version}/products/{filename}.{ext}
     * Public ID resultante: products/{filename}
     */
    private String extractPublicId(String imageUrl) {
        int uploadIdx = imageUrl.indexOf("/upload/");
        if (uploadIdx == -1) {
            return null;
        }
        String afterUpload = imageUrl.substring(uploadIdx + 8);
        // Remover prefijo de version (ej: "v1312461204/")
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        // Remover extension del archivo
        int dotIdx = afterUpload.lastIndexOf('.');
        if (dotIdx != -1) {
            afterUpload = afterUpload.substring(0, dotIdx);
        }
        return afterUpload;
    }
}
