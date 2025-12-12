package com.levelupgamer.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AdultValidatorTest {

    private final AdultValidator validator = new AdultValidator();

    @Test
    void shouldRejectNullBirthdate() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void shouldRejectUnderageUsers() {
        LocalDate seventeenYearsAgo = LocalDate.now().minusYears(17);
        assertFalse(validator.isValid(seventeenYearsAgo, null));
    }

    @Test
    void shouldAcceptAdults() {
        LocalDate twentyYearsAgo = LocalDate.now().minusYears(20);
        assertTrue(validator.isValid(twentyYearsAgo, null));
    }
}
