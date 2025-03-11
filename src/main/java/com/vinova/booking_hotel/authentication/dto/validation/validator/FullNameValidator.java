package com.vinova.booking_hotel.authentication.dto.validation.validator;

import com.vinova.booking_hotel.authentication.dto.validation.annotation.ValidFullName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class FullNameValidator implements ConstraintValidator<ValidFullName, String> {

    // Regex để kiểm tra full name không chứa số và ký tự đặc biệt
    private static final String FULL_NAME_PATTERN = "^[a-zA-Z\\s]+$";

    private final Pattern pattern = Pattern.compile(FULL_NAME_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // cho phép null
        }
        return pattern.matcher(value).matches(); // Kiểm tra full name theo regex
    }
}
