package com.levelupgamer.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Payload;
import java.lang.annotation.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AllowedEmailDomainValidatorTest {

    private final AllowedEmailDomainValidator validator = new AllowedEmailDomainValidator();

    @BeforeEach
    void setUp() {
        validator.initialize(annotationWithDomains("@duoc.cl", "@profesor.duoc.cl", "@gmail.com"));
    }

    @Test
    void shouldAllowNullEmails() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void shouldAcceptAllowedDomainsRegardlessOfCase() {
        assertTrue(validator.isValid("Alumno@DUOC.CL", null));
        assertTrue(validator.isValid("tutor@profesor.duoc.cl", null));
    }

    @Test
    void shouldRejectUnknownDomains() {
        assertFalse(validator.isValid("player@levelupgamer.com", null));
    }

    private AllowedEmailDomain annotationWithDomains(String... domains) {
        return new AllowedEmailDomain() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return AllowedEmailDomain.class;
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
            public String[] domains() {
                return domains;
            }
        };
    }
}
