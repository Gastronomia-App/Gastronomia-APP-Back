package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.business.BusinessNotFoundException;
import com.progra3.cafeteria_api.model.DTO.PublicBusinessDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.entity.Business;
import com.progra3.cafeteria_api.model.entity.Category;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.mapper.CategoryMapper;
import com.progra3.cafeteria_api.model.mapper.ProductMapper;
import com.progra3.cafeteria_api.repository.BusinessRepository;
import com.progra3.cafeteria_api.repository.CategoryRepository;
import com.progra3.cafeteria_api.repository.ProductRepository;
import com.progra3.cafeteria_api.service.port.IPublicMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicMenuService implements IPublicMenuService {

    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getMenuByBusinessSlug(String slug) {
        Business business = businessRepository
                .findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new BusinessNotFoundException(slug));

        List<Category> categories =
                categoryRepository.findByBusiness_SlugAndVisibleInMenuTrueOrderByNameAsc(slug);

        List<Product> products =
                productRepository.findMenuProductsByBusinessSlug(slug);

        Map<Long, List<Product>> productsByCategory = products.stream()
                .filter(Product::isActive)
                .collect(Collectors.groupingBy(p -> p.getCategory().getId()));

        return categories.stream()
                .map(cat -> {
                    CategoryResponseDTO base = categoryMapper.toDTO(cat);

                    List<ProductResponseDTO> productDtos = productsByCategory
                            .getOrDefault(cat.getId(), List.of())
                            .stream()
                            .map(productMapper::toDTO)
                            .toList();

                    return new CategoryResponseDTO(
                            base.id(),
                            base.name(),
                            base.color(),
                            base.icon(),
                            base.visibleInMenu(),
                            productDtos
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicBusinessDTO getBusinessPublicInfo(String slug) {
        Business business = businessRepository
                .findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new BusinessNotFoundException(slug));

        var address = business.getAddress();
        var owner = business.getOwner();

        String phoneNumber = null;
        if (owner != null && owner.getPhoneNumber() != null && !owner.getPhoneNumber().isBlank()) {
            phoneNumber = owner.getPhoneNumber();
        }

        return new PublicBusinessDTO(
                business.getId(),
                business.getName(),
                business.getSlug(),
                phoneNumber,
                address != null ? address.getStreet() : null,
                address != null ? address.getCity() : null,
                address != null ? address.getZipCode() : null,
                address != null ? address.getProvince() : null
        );
    }
}
