package com.levelupgamer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AllowedEmailDomainValidator implements ConstraintValidator<AllowedEmailDomain, String> {

    @Value("${validation.allowed-email-domains}")
    private String[] allowedDomains;

    @Override
    public void initialize(AllowedEmailDomain constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; // O false, si el correo es obligatorio
        }
        String lowerCaseEmail = email.toLowerCase();
        return Arrays.stream(allowedDomains).anyMatch(lowerCaseEmail::endsWith);
    }
}