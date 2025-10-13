package com.progra3.cafeteria_api.validation;

import com.progra3.cafeteria_api.model.dto.BusinessRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBusinessRequestValidator implements ConstraintValidator<ValidBusinessRequest, BusinessRequestDTO> {

    @Override
    public boolean isValid(BusinessRequestDTO dto, ConstraintValidatorContext context) {
        boolean isValid = true;

        if (!dto.cuit().matches("\\d{11}")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CUIT must be an 11-digit number")
                    .addPropertyNode("cuit")
                    .addConstraintViolation();
            isValid = false;
        }

        if (dto.owner() != null && !dto.owner().role().name().equals("OWNER")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Initial employee must have the role 'OWNER'")
                    .addPropertyNode("owner")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
