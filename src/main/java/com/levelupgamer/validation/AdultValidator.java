package com.levelupgamer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    private static final int MIN_AGE = 18;

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) {
            return false;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears() >= MIN_AGE;
    }
}