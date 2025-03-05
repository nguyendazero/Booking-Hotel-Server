package com.vinova.booking_hotel.authentication.validation.validator;

import com.vinova.booking_hotel.authentication.validation.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    // Regex để kiểm tra số điện thoại chỉ chứa số và có độ dài từ 10 đến 15 ký tự
    private static final String PHONE_NUMBER_PATTERN = "^\\d{10,15}$";

    private final Pattern pattern = Pattern.compile(PHONE_NUMBER_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // cho phép null
        }
        return pattern.matcher(value).matches(); // Kiểm tra số điện thoại theo regex
    }
}
