package com.levelupgamer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedEmailDomainValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedEmailDomain {
    String message() default "Dominio de correo no permitido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}