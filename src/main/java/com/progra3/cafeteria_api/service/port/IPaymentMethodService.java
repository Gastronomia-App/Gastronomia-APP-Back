package com.progra3.cafeteria_api.service.port;

import com.progra3.cafeteria_api.model.dto.PaymentMethodRequestDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodResponseDTO;
import com.progra3.cafeteria_api.model.dto.PaymentMethodUpdateDTO;
import com.progra3.cafeteria_api.model.entity.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPaymentMethodService {
    PaymentMethodResponseDTO create(PaymentMethodRequestDTO dto);
    PaymentMethodResponseDTO getById(Long id);
    Page<PaymentMethodResponseDTO> getPaymentMethods(String name, Pageable pageable);
    PaymentMethodResponseDTO update(Long id, PaymentMethodUpdateDTO dto);
    PaymentMethodResponseDTO delete(Long id);
    PaymentMethod getEntityById(Long id);
}

