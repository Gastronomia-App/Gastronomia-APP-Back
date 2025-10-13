package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.model.dto.ProductComponentRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ProductComponentMapper;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductComponent;
import com.progra3.cafeteria_api.service.port.IProductComponentService;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductComponentService implements IProductComponentService {

    private final ProductFinderService productFinderService;

    private final ProductComponentMapper productComponentMapper;

    @Override
    public ProductComponent createProductComponent(ProductComponentRequestDTO dto) {
        Product product = productFinderService.getEntityById(dto.productId());
        ProductComponent productComponent = productComponentMapper.toEntity(dto);
        productComponent.setProduct(product);

        return productComponent;
    }
}
