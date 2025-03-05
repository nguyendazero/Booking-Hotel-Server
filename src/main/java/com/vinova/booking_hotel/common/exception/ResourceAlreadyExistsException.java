package com.vinova.booking_hotel.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;

    public ResourceAlreadyExistsException(String resourceName, String fieldName) {
        super();
        this.resourceName = resourceName;
        this.fieldName = fieldName;
    }
}

