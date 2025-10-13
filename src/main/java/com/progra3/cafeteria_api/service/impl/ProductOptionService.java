package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.model.dto.ProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.mapper.ProductOptionMapper;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.ProductGroup;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.repository.ProductOptionRepository;
import com.progra3.cafeteria_api.service.port.IProductOptionService;
import com.progra3.cafeteria_api.service.helper.Constant;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductOptionService implements IProductOptionService {
    private final ProductOptionRepository productOptionRepository;

    private final ProductFinderService productFinderService;

    private final ProductOptionMapper productOptionMapper;

    @Override
    public ProductOption createProductOption(ProductGroup productGroup, ProductOptionRequestDTO dto) {
        Product product = productFinderService.getEntityById(dto.productId());
        ProductOption productOption = productOptionMapper.toEntity(dto);
        productOption.setProduct(product);
        productOption.setProductGroup(productGroup);
        productOption.setPriceIncrease(dto.priceIncrease() != null ? dto.priceIncrease() : Constant.ZERO_AMOUNT);

        return productOptionRepository.save(productOption);
    }

    @Override
    public ProductOption getEntityById(Long id) {
        return productOptionRepository.findById(id)
                .orElseThrow(() -> new ProductOptionNotFoundException(id));
    }

    @Override
    public ProductOption updateProductOption(ProductOption productOption, ProductOptionRequestDTO dto) {
        productOption = productOptionMapper.updateProductOptionFromDTO(productOption, dto);
        return productOptionRepository.save(productOption);
    }
}
