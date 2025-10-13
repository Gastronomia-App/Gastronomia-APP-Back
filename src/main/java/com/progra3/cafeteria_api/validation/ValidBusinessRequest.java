package com.progra3.cafeteria_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidBusinessRequestValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBusinessRequest {
    String message() default "BusinessRequestDTO is invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
