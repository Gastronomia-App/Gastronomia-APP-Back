package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService {
    @Transactional
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    @Transactional
    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

    @Transactional
    ProductResponseDTO updateProduct(Product updatedProduct);

    @Transactional
    ProductResponseDTO deleteProduct(Long id);

    ProductResponseDTO getProductById(Long id);

    Page<ProductResponseDTO> getProducts(String name, Long categoryId, Integer minStock, Integer maxStock, Boolean composite, Pageable pageable);

    Product getEntityById (Long productId);

    @Transactional
    ProductResponseDTO assignGroupToProduct(Long productId, Long groupId);

    @Transactional
    ProductResponseDTO removeGroupFromProduct(Long productId, Long groupId);

    @Transactional
    ProductResponseDTO  addComponentsToProduct(Long productId, List<ProductComponentRequestDTO> dtos);

    @Transactional
    ProductResponseDTO updateProductComponent(Long productId, Long componentId, Integer quantity);

    @Transactional
    ProductResponseDTO removeComponentFromProduct(Long productId, Long componentId);
}
