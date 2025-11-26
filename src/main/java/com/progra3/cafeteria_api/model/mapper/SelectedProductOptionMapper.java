package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SelectedProductOptionResponseDTO;
import com.progra3.cafeteria_api.model.entity.SelectedProductOption;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), uses = ProductOptionMapper.class)
public interface SelectedProductOptionMapper {

    SelectedProductOptionResponseDTO toDTO(SelectedProductOption selectedProductOption);


}
