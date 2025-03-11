package com.vinova.booking_hotel.authentication.dto.validation.annotation;

import com.vinova.booking_hotel.authentication.dto.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number. It should contain only digits and should be 10 to 15 characters long.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
