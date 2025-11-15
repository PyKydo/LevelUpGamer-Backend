package com.levelupgamer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RutValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Rut {
    String message() default "RUT inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}