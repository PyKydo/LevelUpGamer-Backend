package com.levelupgamer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RutValidator implements ConstraintValidator<Rut, String> {

    @Override
    public boolean isValid(String rut, ConstraintValidatorContext context) {
        if (rut == null || rut.isEmpty()) {
            return true; // NotBlank se encarga de la obligatoriedad
        }

        try {
            rut = rut.toUpperCase().replace(".", "").replace("-", "");
            int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));
            char dv = rut.charAt(rut.length() - 1);

            int m = 0, s = 1;
            for (; rutAux != 0; rutAux /= 10) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
            }
            
            char expectedDv = (char) (s != 0 ? s + 47 : 75);
            
            return dv == expectedDv;
        } catch (Exception e) {
            return false;
        }
    }
}
