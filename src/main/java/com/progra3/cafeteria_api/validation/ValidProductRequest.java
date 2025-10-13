package com.progra3.cafeteria_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidProductRequestValidator.class)
@Documented
public @interface ValidProductRequest {
    String message() default "Invalid product request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}