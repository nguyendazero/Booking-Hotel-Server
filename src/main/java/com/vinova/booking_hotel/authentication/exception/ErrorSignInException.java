package com.vinova.booking_hotel.authentication.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ErrorSignInException extends RuntimeException{

    public ErrorSignInException() {
        super();
    }

}

