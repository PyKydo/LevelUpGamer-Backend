package com.levelupgamer.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RutValidatorTest {

    private final RutValidator validator = new RutValidator();

    @Test
    void shouldAcceptValidRutWithFormatting() {
        assertTrue(validator.isValid("76.086.428-5", null));
    }

    @Test
    void shouldRejectInvalidVerificationDigit() {
        assertFalse(validator.isValid("76086428-4", null));
    }

    @Test
    void shouldAllowEmptyRut() {
        assertTrue(validator.isValid("", null));
    }

    @Test
    void shouldRejectWhenFormatIsInvalid() {
        assertFalse(validator.isValid("ABC123", null));
    }
}
