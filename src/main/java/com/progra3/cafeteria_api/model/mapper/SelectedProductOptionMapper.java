package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SelectedProductOptionRequestDTO;
import com.progra3.cafeteria_api.model.dto.SelectedProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.SelectedProductOption;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SelectedProductOptionMapper {
    @Mapping(target = "productOptionId", source = "productOption.id")
    SelectedProductOptionResponseDTO toDTO(SelectedProductOption selectedProductOption);

    @Mapping(target = "productOption.id", source = "productOptionId")
    List<SelectedProductOption> toEntityList(List<SelectedProductOptionRequestDTO> dtos);

    @Mapping(target = "productOption.id", source = "productOptionId")
    SelectedProductOption toEntity(SelectedProductOptionRequestDTO dto);

}
