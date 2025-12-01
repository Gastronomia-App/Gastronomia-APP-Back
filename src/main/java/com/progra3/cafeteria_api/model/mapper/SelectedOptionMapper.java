package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.exception.product.ProductOptionNotFoundException;
import com.progra3.cafeteria_api.model.dto.SelectedOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.ProductOption;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import com.progra3.cafeteria_api.repository.ProductOptionRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = ProductOptionMapper.class)
public abstract class SelectedOptionMapper {

    @Autowired
    protected ProductOptionRepository productOptionRepository;

    // --- TO DTO ---

    @Mapping(target = "productOption", source = "productOption") // Uses toProductOptionDTO below
    public abstract SelectedOptionResponseDTO toDTO(SelectedOption entity);

    // --- TO ENTITY ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true) // The Root Item is set ONLY on level 1 options in ItemMapper
    @Mapping(target = "parentOption", ignore = true) // Parent link handled in AfterMapping
    @Mapping(target = "productOption", source = "productOptionId", qualifiedByName = "mapProductOption")
    @Mapping(target = "selectedOptions", source = "selectedOptions") // Recursive mapping happens automatically
    public abstract SelectedOption toEntity(SelectedOptionRequestDTO dto);

    // --- LOGIC & HELPERS ---

    /**
     * Resolves the ProductOption entity from the Database using the ID provided in DTO.
     */
    @Named("mapProductOption")
    protected ProductOption mapProductOption(Long id) {
        return productOptionRepository.findById(id)
                .orElseThrow(() -> new ProductOptionNotFoundException(id));
    }

    /**
     * Post-processing to establish parent-child relationships within the options tree.
     * Note: The 'Item' (Root) reference is set ONLY on level 1 options in ItemMapper.
     * Nested options (level 2+) will NOT have item set - they only know their parentOption.
     */
    @AfterMapping
    protected void linkOptionRelationships(@MappingTarget SelectedOption parent) {
        if (parent.getSelectedOptions() != null) {
            parent.getSelectedOptions().forEach(child -> {
                // Set the immediate parent
                child.setParentOption(parent);
                // Recurse down the tree
                linkOptionRelationships(child);
            });
        }
    }
}