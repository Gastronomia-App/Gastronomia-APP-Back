package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.seating.SeatingAlreadyExistsException;
import com.progra3.cafeteria_api.exception.seating.SeatingModificationNotAllowed;
import com.progra3.cafeteria_api.exception.seating.SeatingNotFoundException;
import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import com.progra3.cafeteria_api.model.mapper.SeatingMapper;
import com.progra3.cafeteria_api.repository.SeatingRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.ISeatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatingService implements ISeatingService {

    private final SeatingRepository seatingRepository;

    private final EmployeeContext employeeContext;

    private final SeatingMapper seatingMapper;

    @Override
    public SeatingResponseDTO create(SeatingRequestDTO seatingRequestDTO) {
        Seating seating = seatingMapper.toEntity(seatingRequestDTO);
        seating.setBusiness(employeeContext.getCurrentBusiness());

        seatingRepository.findByNumberAndBusiness_Id(seating.getNumber(), employeeContext.getCurrentBusinessId())
                .ifPresent(s -> {
                    if(!s.getDeleted()) throw new SeatingAlreadyExistsException(seating.getNumber());
                });
        seating.setStatus(SeatingStatus.FREE);
        seating.setDeleted(false);
        return seatingMapper.toDTO(seatingRepository.save(seating));
    }

    @Override
    public SeatingResponseDTO getById(Long seatingId) {
        return seatingMapper.toDTO(getEntityById(seatingId));
    }

    @Override
    public Seating getEntityById(Long id) {
        return Optional.ofNullable(id)
                .map(customer -> seatingRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                        .orElseThrow(() -> new SeatingNotFoundException(id)))
                .orElse(null);
    }

    @Override
    public List<SeatingResponseDTO> getAll() {
        return seatingRepository.findByBusiness_Id(employeeContext.getCurrentBusinessId())
                .stream()
                .map(seatingMapper::toDTO)
                .toList();
    }

    @Override
    public SeatingResponseDTO updateNumber(Long id, SeatingRequestDTO dto) {
        Seating seating = getEntityById(id);

        validateSeating(seating);

        seating = seatingMapper.updateSeatingFromDTO(seating, dto);

        return seatingMapper.toDTO(seatingRepository.save(seating));
    }

    @Override
    public void updateStatus(Seating seating, OrderStatus status) {

        validateSeating(seating);

        SeatingStatus newSeatingStatus = switch (status) {
            case BILLED -> SeatingStatus.BILLING;
            case FINALIZED, CANCELED -> SeatingStatus.FREE;
            default -> null;
        };

        seating.setStatus(newSeatingStatus);

        seatingRepository.save(seating);
    }

    @Override
    public SeatingResponseDTO delete(Long id) {
        Seating seating = getEntityById(id);

        if(seating.getStatus() != SeatingStatus.FREE) {
            throw new SeatingModificationNotAllowed("Seating cannot be deleted while it is not free.");
        }

        seating.setStatus(SeatingStatus.DELETED);
        seating.setDeleted(true);

        return seatingMapper.toDTO(seatingRepository.save(seating));
    }

    @Override
    public SeatingResponseDTO getByNumber(Integer number) {
        return seatingMapper.toDTO(seatingRepository.findByNumberAndBusiness_Id(number, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new SeatingNotFoundException(number)));
    }

    private void validateSeating(Seating seating) {
        if (seating.getDeleted()) {
            throw new SeatingModificationNotAllowed("Seating is deleted and cannot be modified.");
        }
    }
}
