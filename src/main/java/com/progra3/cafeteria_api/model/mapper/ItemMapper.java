package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.ItemRequestDTO;
import com.progra3.cafeteria_api.model.dto.ItemResponseDTO;
import com.progra3.cafeteria_api.model.entity.Item;
import com.progra3.cafeteria_api.model.entity.Order;
import com.progra3.cafeteria_api.model.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = {SelectedProductOptionMapper.class})
public interface ItemMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "orderId", source = "order.id")
    ItemResponseDTO toDTO(Item item);

    Item toEntity(ItemRequestDTO dto);

    Item updateItemFromDTO(ItemRequestDTO itemDTO, @MappingTarget Item item);
}
