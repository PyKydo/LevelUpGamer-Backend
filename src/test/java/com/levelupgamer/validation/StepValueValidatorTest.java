package com.levelupgamer.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Payload;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StepValueValidatorTest {

    private final StepValueValidator validator = new StepValueValidator();

    @BeforeEach
    void setUp() {
        validator.initialize(stepValue(5, 0, 20));
    }

    @Test
    void shouldAllowNullValues() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldRejectValuesBelowMin() {
        assertFalse(validator.isValid(-5, null));
    }

    @Test
    void shouldRejectValuesAboveMax() {
        assertFalse(validator.isValid(25, null));
    }

    @Test
    void shouldAcceptValuesAlignedWithStep() {
        assertTrue(validator.isValid(15, null));
    }

    @Test
    void shouldRejectValuesThatDoNotFollowStep() {
        assertFalse(validator.isValid(16, null));
    }

    private StepValue stepValue(int step, int min, int max) {
        return new StepValue() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return StepValue.class;
            }

            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class<?>[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public int step() {
                return step;
            }

            @Override
            public int min() {
                return min;
            }

            @Override
            public int max() {
                return max;
            }
        };
    }
}
