package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.event.SeatingAllOccupiedEvent;
import com.progra3.cafeteria_api.event.SeatingAvailableEvent;
import com.progra3.cafeteria_api.exception.seating.SeatingAlreadyExistsException;
import com.progra3.cafeteria_api.exception.seating.SeatingModificationNotAllowed;
import com.progra3.cafeteria_api.exception.seating.SeatingNotFoundException;
import com.progra3.cafeteria_api.model.dto.SeatingRequestDTO;
import com.progra3.cafeteria_api.model.dto.SeatingResponseDTO;
import com.progra3.cafeteria_api.model.dto.SeatingPositionRequestDTO;
import com.progra3.cafeteria_api.model.entity.Seating;
import com.progra3.cafeteria_api.model.enums.OrderStatus;
import com.progra3.cafeteria_api.model.enums.SeatingStatus;
import com.progra3.cafeteria_api.model.mapper.SeatingMapper;
import com.progra3.cafeteria_api.repository.SeatingRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.model.dto.websocket.DataChangeEvent;
import com.progra3.cafeteria_api.service.port.ISeatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatingService implements ISeatingService {

    private final SeatingRepository seatingRepository;
    private final EmployeeContext employeeContext;
    private final SeatingMapper seatingMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public SeatingResponseDTO create(SeatingRequestDTO dto) {
        Long businessId = employeeContext.getCurrentBusinessId();

        Optional<Seating> existingOpt = seatingRepository.findByNumberAndBusiness_Id(dto.number(), businessId);

        Seating seating;

        if (existingOpt.isPresent()) {
            Seating existing = existingOpt.get();

            if (!existing.getDeleted()) {
                throw new SeatingAlreadyExistsException(dto.number());
            }

            seating = existing;
            seatingMapper.updateSeatingFromDTO(seating, dto);
            seating.setDeleted(false);
            seating.setStatus(SeatingStatus.FREE);

        } else {
            seating = seatingMapper.toEntity(dto);
            seating.setBusiness(employeeContext.getCurrentBusiness());
            seating.setStatus(SeatingStatus.FREE);
            seating.setDeleted(false);
        }

        if (positionTaken(seating, seating.getId())) {
            throw new SeatingModificationNotAllowed("There is already a seating in this position.");
        }

        SeatingResponseDTO result = seatingMapper.toDTO(seatingRepository.save(seating));
        messagingTemplate.convertAndSend("/topic/data-changes/" + employeeContext.getCurrentBusinessId(), new DataChangeEvent("SEATING"));
        return result;
    }

    @Override
    public SeatingResponseDTO getById(Long seatingId) {
        return seatingMapper.toDTO(getEntityById(seatingId));
    }

    @Override
    public Seating getEntityById(Long id) {
        return Optional.ofNullable(id)
                .map(seatingId -> seatingRepository.findByIdAndBusiness_Id(seatingId, employeeContext.getCurrentBusinessId())
                        .orElseThrow(() -> new SeatingNotFoundException(id)))
                .orElse(null);
    }

    @Override
    public List<SeatingResponseDTO> getAll() {
        return seatingRepository.findActiveByBusiness_Id(employeeContext.getCurrentBusinessId())
                .stream()
                .map(seatingMapper::toDTO)
                .toList();
    }

    @Override
    public SeatingResponseDTO updateSeating(Long id, SeatingRequestDTO dto) {
        Seating seating = getEntityById(id);
        validateSeating(seating);

        seatingRepository.findByNumberAndBusiness_Id(dto.number(), employeeContext.getCurrentBusinessId())
                .ifPresent(existing -> {
                    boolean isSameSeating = existing.getId().equals(seating.getId());
                    if (!isSameSeating) {
                        if (!existing.getDeleted()) {
                            throw new SeatingAlreadyExistsException(dto.number());
                        } else {
                            throw new SeatingModificationNotAllowed("Cannot use a number from a deleted seating.");
                        }
                    }
                });

        seatingMapper.updateSeatingFromDTO(seating, dto);

        if (positionTaken(seating, seating.getId())) {
            throw new SeatingModificationNotAllowed("There is already a seating in this position.");
        }

        SeatingResponseDTO result = seatingMapper.toDTO(seatingRepository.save(seating));
        messagingTemplate.convertAndSend("/topic/data-changes/" + employeeContext.getCurrentBusinessId(), new DataChangeEvent("SEATING"));
        return result;
    }

    @Override
    public void updateStatus(Seating seating, OrderStatus status) {
        validateSeating(seating);

        SeatingStatus oldStatus = seating.getStatus();
        SeatingStatus newStatus = switch (status) {
            case BILLED -> SeatingStatus.BILLING;
            case FINALIZED, CANCELED -> SeatingStatus.FREE;
            default -> seating.getStatus();
        };

        seating.setStatus(newStatus);
        seatingRepository.save(seating);

        messagingTemplate.convertAndSend("/topic/data-changes/" + employeeContext.getCurrentBusinessId(), new DataChangeEvent("SEATING"));
        checkSeatingAvailability(oldStatus, newStatus, seating);
    }

    @Override
    public SeatingResponseDTO delete(Long id) {
        Seating seating = getEntityById(id);

        if (seating.getStatus() != SeatingStatus.FREE) {
            throw new SeatingModificationNotAllowed("Seating cannot be deleted while it is not free.");
        }

        Long businessId = employeeContext.getCurrentBusinessId();
        seating.setStatus(SeatingStatus.DELETED);
        seating.setDeleted(true);

        SeatingResponseDTO result = seatingMapper.toDTO(seatingRepository.save(seating));
        messagingTemplate.convertAndSend("/topic/data-changes/" + businessId, new DataChangeEvent("SEATING"));
        return result;
    }

    @Override
    public SeatingResponseDTO getByNumber(Integer number) {
        return seatingMapper.toDTO(
                seatingRepository.findByNumberAndBusiness_Id(number, employeeContext.getCurrentBusinessId())
                        .orElseThrow(() -> new SeatingNotFoundException(number))
        );
    }

    private void validateSeating(Seating seating) {
        if (seating.getDeleted()) {
            throw new SeatingModificationNotAllowed("Seating is deleted and cannot be modified.");
        }
    }

    private boolean positionTaken(Seating seating, Long editingId) {
        return seatingRepository.findByBusiness_Id(employeeContext.getCurrentBusinessId())
                .stream()
                .filter(s -> !s.getDeleted())
                .filter(s -> editingId == null || !s.getId().equals(editingId))
                .anyMatch(s ->
                        s.getPosX().equals(seating.getPosX()) &&
                                s.getPosY().equals(seating.getPosY())
                );
    }

    @Override
    public SeatingResponseDTO updatePosition(Long id, SeatingPositionRequestDTO request) {
        Seating seating = seatingRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new SeatingNotFoundException(id));

        validateSeating(seating);

        seating.setPosX(request.posX());
        seating.setPosY(request.posY());

        seatingRepository.save(seating);
        messagingTemplate.convertAndSend("/topic/data-changes/" + employeeContext.getCurrentBusinessId(), new DataChangeEvent("SEATING"));
        return seatingMapper.toDTO(seating);
    }


    // Notification methods
    public void checkAndNotifySeatingOccupancy() {
        Long businessId = employeeContext.getCurrentBusinessId();
        List<Seating> allSeatings = seatingRepository.findActiveByBusiness_Id(businessId);

        long freeCount = allSeatings.stream()
                .filter(s -> s.getStatus() == SeatingStatus.FREE)
                .count();

        boolean allOccupied = (freeCount == 0);

        if (allOccupied) {
            eventPublisher.publishEvent(new SeatingAllOccupiedEvent(businessId));
        }
    }

    private void checkSeatingAvailability(SeatingStatus oldStatus, SeatingStatus newStatus, Seating seating) {
        Long businessId = employeeContext.getCurrentBusinessId();

        if (oldStatus != SeatingStatus.FREE && newStatus == SeatingStatus.FREE) {
            eventPublisher.publishEvent(new SeatingAvailableEvent(seating, businessId));
        }

        if (oldStatus == SeatingStatus.FREE && newStatus != SeatingStatus.FREE) {
            List<Seating> allSeatings = seatingRepository.findActiveByBusiness_Id(businessId);

            long freeCount = allSeatings.stream()
                    .filter(s -> s.getStatus() == SeatingStatus.FREE)
                    .count();

            boolean allOccupied = (freeCount == 0);

            if (allOccupied) {
                eventPublisher.publishEvent(new SeatingAllOccupiedEvent(businessId));
            }
        }
    }
}
