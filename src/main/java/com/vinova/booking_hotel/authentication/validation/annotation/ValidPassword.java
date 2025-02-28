package com.vinova.booking_hotel.authentication.validation.annotation;

import com.vinova.booking_hotel.authentication.validation.validator.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Invalid password.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
