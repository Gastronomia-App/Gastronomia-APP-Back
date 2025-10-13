package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SeatingMapper {
    SeatingResponseDTO toDTO(Seating seating);

    Seating toEntity(SeatingRequestDTO seatingRequestDTO);

    Seating updateSeatingFromDTO(@MappingTarget Seating seating, SeatingRequestDTO seatingRequestDTO);
}
