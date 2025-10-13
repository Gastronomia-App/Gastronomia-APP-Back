package com.progra3.cafeteria_api.validation;

import com.progra3.cafeteria_api.model.dto.OrderRequestDTO;
import com.progra3.cafeteria_api.model.enums.OrderType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidOrderRequestValidator implements ConstraintValidator<ValidOrderRequest, OrderRequestDTO> {

    @Override
    public boolean isValid(OrderRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.orderType() == null) return false;

        if (dto.orderType() == OrderType.TABLE) {
            boolean validSeatingId = dto.seatingId() != null;
            boolean validPeople = dto.peopleCount() != null && dto.peopleCount() > 0;

            if (!validSeatingId || !validPeople) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Seating orders must include seatingId and peopleCount > 0")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}