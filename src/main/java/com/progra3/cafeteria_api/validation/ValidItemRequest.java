package com.progra3.cafeteria_api.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidItemRequestValidator.class)
@Documented
public @interface ValidItemRequest {
    String message() default "Invalid item request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}