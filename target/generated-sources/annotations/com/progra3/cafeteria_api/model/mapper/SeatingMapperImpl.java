package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-17T00:03:10-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 24.0.1 (Oracle Corporation)"
)
@Component
public class SeatingMapperImpl implements SeatingMapper {

    @Override
    public SeatingResponseDTO toDTO(Seating seating) {
        if ( seating == null ) {
            return null;
        }

        Long id = null;
        Integer number = null;
        SeatingStatus status = null;
        Boolean deleted = null;

        id = seating.getId();
        number = seating.getNumber();
        status = seating.getStatus();
        deleted = seating.getDeleted();

        SeatingResponseDTO seatingResponseDTO = new SeatingResponseDTO( id, number, status, deleted );

        return seatingResponseDTO;
    }

    @Override
    public Seating toEntity(SeatingRequestDTO seatingRequestDTO) {
        if ( seatingRequestDTO == null ) {
            return null;
        }

        Seating seating = new Seating();

        seating.setNumber( seatingRequestDTO.number() );

        return seating;
    }

    @Override
    public Seating updateSeatingFromDTO(Seating seating, SeatingRequestDTO seatingRequestDTO) {
        if ( seatingRequestDTO == null ) {
            return seating;
        }

        if ( seatingRequestDTO.number() != null ) {
            seating.setNumber( seatingRequestDTO.number() );
        }

        return seating;
    }
}
