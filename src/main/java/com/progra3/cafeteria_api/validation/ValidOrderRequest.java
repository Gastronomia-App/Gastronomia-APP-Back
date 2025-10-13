package com.progra3.cafeteria_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidOrderRequestValidator.class)
@Documented
public @interface ValidOrderRequest {
    String message() default "Invalid destinationOrder request based on destinationOrder type";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}