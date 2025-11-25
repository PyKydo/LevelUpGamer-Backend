package com.levelupgamer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class AllowedEmailDomainValidator implements ConstraintValidator<AllowedEmailDomain, String> {

    private String[] allowedDomains;

    @Override
    public void initialize(AllowedEmailDomain constraintAnnotation) {
        this.allowedDomains = constraintAnnotation.domains();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; 
        }
        String lowerCaseEmail = email.toLowerCase();
        return Arrays.stream(allowedDomains).anyMatch(lowerCaseEmail::endsWith);
    }
}