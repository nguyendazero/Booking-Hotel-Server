package com.vinova.booking_hotel.authentication.dto.validation.annotation;

import com.vinova.booking_hotel.authentication.dto.validation.validator.FullNameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FullNameValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFullName {
    String message() default "Invalid full name. It should not contain numbers or special characters.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
