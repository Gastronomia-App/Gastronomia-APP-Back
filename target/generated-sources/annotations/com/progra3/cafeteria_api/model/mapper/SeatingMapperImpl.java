package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.OrderResponseDTO;
import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.SeatingShape;
import com.progra3.cafeteria_api.model.enums.SeatingSize;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-07T13:54:46-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Oracle Corporation)"
)
@Component
public class SeatingMapperImpl implements SeatingMapper {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public SeatingResponseDTO toDTO(Seating seating) {
        if ( seating == null ) {
            return null;
        }

        Long id = null;
        Integer number = null;
        Integer posX = null;
        Integer posY = null;
        SeatingShape shape = null;
        SeatingSize size = null;
        SeatingStatus status = null;
        Boolean deleted = null;
        OrderResponseDTO activeOrder = null;

        id = seating.getId();
        number = seating.getNumber();
        posX = seating.getPosX();
        posY = seating.getPosY();
        shape = seating.getShape();
        size = seating.getSize();
        status = seating.getStatus();
        deleted = seating.getDeleted();
        activeOrder = orderMapper.toDTO( seating.getActiveOrder() );

        SeatingResponseDTO seatingResponseDTO = new SeatingResponseDTO( id, number, posX, posY, shape, size, status, deleted, activeOrder );

        return seatingResponseDTO;
    }

    @Override
    public Seating toEntity(SeatingRequestDTO seatingRequestDTO) {
        if ( seatingRequestDTO == null ) {
            return null;
        }

        Seating seating = new Seating();

        seating.setNumber( seatingRequestDTO.number() );
        seating.setPosX( seatingRequestDTO.posX() );
        seating.setPosY( seatingRequestDTO.posY() );
        seating.setShape( seatingRequestDTO.shape() );
        seating.setSize( seatingRequestDTO.size() );

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
        if ( seatingRequestDTO.posX() != null ) {
            seating.setPosX( seatingRequestDTO.posX() );
        }
        if ( seatingRequestDTO.posY() != null ) {
            seating.setPosY( seatingRequestDTO.posY() );
        }
        if ( seatingRequestDTO.shape() != null ) {
            seating.setShape( seatingRequestDTO.shape() );
        }
        if ( seatingRequestDTO.size() != null ) {
            seating.setSize( seatingRequestDTO.size() );
        }

        return seating;
    }
}
