package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import java.util.List;

public interface ISeatingService {
    SeatingResponseDTO create(SeatingRequestDTO seatingRequestDTO);
    List<SeatingResponseDTO> getAll();
    SeatingResponseDTO getById(Long id);
    SeatingResponseDTO getByNumber(Integer number);
    SeatingResponseDTO updateNumber(Long id, SeatingRequestDTO dto);
    void updateStatus(Seating seating, OrderStatus status);
    SeatingResponseDTO delete(Long id);

    Seating getEntityById(Long id);
}
