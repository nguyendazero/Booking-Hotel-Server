package com.vinova.booking_hotel.authentication.validation.validator;

import com.vinova.booking_hotel.authentication.validation.annotation.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Ít nhất 8 ký tự, có chữ hoa, chữ thường, ký tự số và ký tự đặc biệt
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\W).{8,}$"; 

    private final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // Không cho phép null
        }
        return pattern.matcher(value).matches(); // Kiểm tra mật khẩu theo regex
    }
}
