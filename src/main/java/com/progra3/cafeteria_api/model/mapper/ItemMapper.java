package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Product;
import com.progra3.cafeteria_api.model.entity.SelectedOption;
import com.progra3.cafeteria_api.service.helper.ProductFinderService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        uses = {SelectedOptionMapper.class, ProductMapper.class}
)
public abstract class ItemMapper {

    @Autowired
    protected ProductFinderService productFinderService;

    // --- TO DTO ---

    @Mapping(target = "orderId", source = "order.id")
    // selectedOptions are mapped automatically using SelectedOptionMapper.toDTO
    public abstract ItemResponseDTO toDTO(Item item);

    // --- TO ENTITY ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "product", source = "productId", qualifiedByName = "mapProduct")
    public abstract Item toEntity(ItemRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "unitPrice", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    public abstract Item updateItemFromDTO(ItemRequestDTO dto, @MappingTarget Item item);

    // --- LOGIC & HELPERS ---

    @Named("mapProduct")
    protected Product mapProduct(Long id) {
        return productFinderService.getEntityById(id);
    }

    /**
     * CRITICAL: Establishes the relationship between the Root Item and ALL its selected options (recursively).
     * SelectedOptionMapper handles Option<->Option links, but ItemMapper must assign the 'item' owner.
     */
    @AfterMapping
    protected void linkItemToOptions(@MappingTarget Item item) {
        if (item.getSelectedOptions() != null) {
            item.getSelectedOptions().forEach(option -> {
                // Link Level 1 options
                option.setItem(item);
                // Recursively link deep children
                setRecursiveRootItem(option, item);
            });
        }
    }

    private void setRecursiveRootItem(SelectedOption option, Item rootItem) {
        if (option.getSelectedOptions() != null) {
            option.getSelectedOptions().forEach(child -> {
                child.setItem(rootItem); // Assign the root item owner
                setRecursiveRootItem(child, rootItem); // Continue down the tree
            });
        }
    }
}