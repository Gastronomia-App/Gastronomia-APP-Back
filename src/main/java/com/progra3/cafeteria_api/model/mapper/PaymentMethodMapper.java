package com.progra3.cafeteria_api.model.mapper;

import com.progra3.cafeteria_api.model.dto.PaymentMethodRequestDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodResponseDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodUpdateDTO;
import com.progra3.cafeteria_api.model.entity.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {

    public PaymentMethod toEntity(PaymentMethodRequestDTO dto) {
        return PaymentMethod.builder()
                .name(dto.name())
                .description(dto.description())
                .build();
    }

    public PaymentMethodResponseDTO toDTO(PaymentMethod paymentMethod) {
        return new PaymentMethodResponseDTO(
                paymentMethod.getId(),
                paymentMethod.getName(),
                paymentMethod.getDescription()
        );
    }

    public void updatePaymentMethodFromDTO(PaymentMethodUpdateDTO dto, PaymentMethod paymentMethod) {
        if (dto.name() != null && !dto.name().isBlank()) {
            paymentMethod.setName(dto.name());
        }
        if (dto.description() != null) {
            paymentMethod.setDescription(dto.description());
        }
    }
}

