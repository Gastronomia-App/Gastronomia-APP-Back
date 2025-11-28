package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodNameAlreadyExistsException;
import com.progra3.cafeteria_api.exception.paymentmethod.PaymentMethodNotFoundException;
import com.progra3.cafeteria_api.model.dto.PaymentMethodRequestDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodResponseDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodUpdateDTO;
import com.progra3.cafeteria_api.model.entity.PaymentMethod;
import com.progra3.cafeteria_api.model.mapper.PaymentMethodMapper;
import com.progra3.cafeteria_api.repository.PaymentMethodRepository;
import com.progra3.cafeteria_api.security.EmployeeContext;
import com.progra3.cafeteria_api.service.port.IPaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentMethodService implements IPaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final EmployeeContext employeeContext;
    private final PaymentMethodMapper paymentMethodMapper;

    @Override
    @Transactional
    public PaymentMethodResponseDTO create(PaymentMethodRequestDTO dto) {
        PaymentMethod paymentMethod = paymentMethodMapper.toEntity(dto);
        paymentMethod.setBusiness(employeeContext.getCurrentBusiness());

        Optional<PaymentMethod> existingPaymentMethod = validatePaymentMethod(paymentMethod);

        if (existingPaymentMethod.isPresent()) {
            paymentMethod = existingPaymentMethod.get();
            paymentMethod.setName(dto.name());
            paymentMethod.setDescription(dto.description());
        }

        paymentMethod.setDeleted(false);

        return paymentMethodMapper.toDTO(paymentMethodRepository.save(paymentMethod));
    }

    @Override
    public PaymentMethodResponseDTO getById(Long id) {
        PaymentMethod paymentMethod = getEntityById(id);
        return paymentMethodMapper.toDTO(paymentMethod);
    }

    @Override
    public Page<PaymentMethodResponseDTO> getPaymentMethods(String name, Pageable pageable) {
        Page<PaymentMethod> paymentMethods = paymentMethodRepository.findByBusiness_Id(
                name,
                employeeContext.getCurrentBusinessId(),
                pageable);

        return paymentMethods.map(paymentMethodMapper::toDTO);
    }

    @Override
    @Transactional
    public PaymentMethodResponseDTO update(Long id, PaymentMethodUpdateDTO dto) {
        Long businessId = employeeContext.getCurrentBusinessId();
        PaymentMethod paymentMethod = getEntityById(id);

        // Validate name uniqueness if changed
        if (dto.name() != null && !dto.name().equals(paymentMethod.getName())) {
            if (paymentMethodRepository.existsByNameAndBusiness_Id(dto.name(), businessId)) {
                throw new PaymentMethodNameAlreadyExistsException(dto.name());
            }
        }

        paymentMethodMapper.updatePaymentMethodFromDTO(dto, paymentMethod);

        return paymentMethodMapper.toDTO(paymentMethodRepository.save(paymentMethod));
    }

    @Override
    @Transactional
    public PaymentMethodResponseDTO delete(Long id) {
        PaymentMethod paymentMethod = getEntityById(id);
        paymentMethod.setDeleted(true);

        return paymentMethodMapper.toDTO(paymentMethodRepository.save(paymentMethod));
    }

    @Override
    public PaymentMethod getEntityById(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findByIdAndBusiness_Id(id, employeeContext.getCurrentBusinessId())
                .orElseThrow(() -> new PaymentMethodNotFoundException(id));

        if (paymentMethod.getDeleted()) {
            throw new PaymentMethodNotFoundException(id);
        }

        return paymentMethod;
    }

    private Optional<PaymentMethod> validatePaymentMethod(PaymentMethod paymentMethod) {
        Optional<PaymentMethod> optionalPaymentMethod = paymentMethodRepository.findByNameAndBusiness_Id(
                paymentMethod.getName(),
                employeeContext.getCurrentBusinessId());

        if (optionalPaymentMethod.isPresent()) {
            if (!optionalPaymentMethod.get().getDeleted()) {
                throw new PaymentMethodNameAlreadyExistsException(paymentMethod.getName());
            }
            return optionalPaymentMethod;
        }

        return Optional.empty();
    }
}

