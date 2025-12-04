package com.progra3.cafeteria_api.service.port;

import org.springframework.web.multipart.MultipartFile;

public interface IProductImageStorageService {

    /**
     * Stores a new product image and returns the public URL
     * that can be persisted in the Product entity.
     */
    String store(MultipartFile file);

    /**
     * Deletes the image file associated with the given URL, if it exists.
     * Implementations should ignore null or blank URLs.
     */
    void delete(String imageUrl);
}