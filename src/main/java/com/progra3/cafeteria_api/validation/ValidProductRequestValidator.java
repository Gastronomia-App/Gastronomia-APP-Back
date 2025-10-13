package com.progra3.cafeteria_api.validation;

import com.progra3.cafeteria_api.model.dto.ProductRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidProductRequestValidator implements ConstraintValidator<ValidProductRequest, ProductRequestDTO> {

    @Override
    public boolean isValid(ProductRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null || dto.name() == null || dto.name().isBlank() || dto.price() == null || dto.controlStock() == null) {
            return false;
        }

        if (dto.categoryId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Product must have a valid categoryId")
                    .addPropertyNode("categoryId")
                    .addConstraintViolation();
            return false;
        }

        if (dto.price() < 0.0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Product price must be greater than zero")
                    .addPropertyNode("price")
                    .addConstraintViolation();
            return false;
        }

        if (dto.controlStock()) {
            if (dto.stock() == null){
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Product stock must be set when controlStock is true")
                        .addPropertyNode("stock")
                        .addConstraintViolation();
                return false;
            }

            if (dto.stock() < 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Product stock must be greater than or equal to zero")
                        .addPropertyNode("stock")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
