package com.vinova.booking_hotel.authentication.exception;

import com.vinova.booking_hotel.authentication.dto.response.ErrorDetail;
import com.vinova.booking_hotel.authentication.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(HttpServletRequest request, ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("404");
        errorDetail.setErrorMessageId("NOTFOUND4041E");
        errorDetail.setErrorMessage("Resource not found: " + ex.getResourceName() + " with " + ex.getFieldName() + " = " + ex.getFieldValue());

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ErrorSignInException.class)
    public ResponseEntity<ErrorResponse> handleErrorSignInException(HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("400");
        errorDetail.setErrorMessageId("ERRORSIGNIN");
        String errorMessage = "Invalid username or password";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotMatchPasswordException.class)
    public ResponseEntity<ErrorResponse> handleNotMatchPasswordException(HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("400");
        errorDetail.setErrorMessageId("NOTMATCHPASSWORD");
        String errorMessage = "Password not match";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccountIsBlockException.class)
    public ResponseEntity<ErrorResponse> handleAccountIsBlockException(HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("400");
        errorDetail.setErrorMessageId("ACCOUNTISBLOCKED");
        String errorMessage = "Account is blocked";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(HttpServletRequest request, ResourceAlreadyExistsException ex) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.CONFLICT.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("409");
        errorDetail.setErrorMessageId("RESOURCEALREADYEXISTS");
        errorDetail.setErrorMessage("Resource already exists: " + ex.getResourceName() + " with " + ex.getFieldName() + " = " + ex.getFieldValue());

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InValidVerifyEmailException.class)
    public ResponseEntity<ErrorResponse> handleInValidVerifyEmailException(HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode("400");
        errorDetail.setErrorMessageId("INVALIDVERIFYEMAIL");
        String errorMessage = "Invalid verify email";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
}
