package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.dto.ProductResponseDTO;
import com.progra3.cafeteria_api.model.dto.CategoryResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {SelectedProductOptionMapper.class})
public interface ItemMapper {
    @Mapping(target = "product", source = "product", qualifiedByName = "productWithoutCircularRefs")
    @Mapping(target = "orderId", source = "order.id")
    ItemResponseDTO toDTO(Item item);

    Item toEntity(ItemRequestDTO dto);

    Item updateItemFromDTO(ItemRequestDTO itemDTO, @MappingTarget Item item);

    @Named("productWithoutCircularRefs")
    default ProductResponseDTO productWithoutCircularRefs(Product product) {
        if (product == null) {
            return null;
        }

        CategoryResponseDTO category = null;
        if (product.getCategory() != null) {
            category = CategoryResponseDTO.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .products(null)  // Evitar recursión
                    .build();
        }

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .cost(product.getCost())
                .controlStock(product.isControlStock())
                .stock(product.getStock())
                .category(category)
                .active(product.isActive())
                .composite(product.isComposite())
                .components(null)  // No incluir componentes en items
                .productGroups(null)  // No incluir grupos en items
                .build();
    }
}
