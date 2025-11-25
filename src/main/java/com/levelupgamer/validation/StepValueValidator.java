package com.levelupgamer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StepValueValidator implements ConstraintValidator<StepValue, Integer> {
    private int step;
    private int min;
    private int max;

    @Override
    public void initialize(StepValue constraintAnnotation) {
        this.step = constraintAnnotation.step();
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value < min || value > max) {
            return false;
        }
        return (value - min) % step == 0;
    }
}
