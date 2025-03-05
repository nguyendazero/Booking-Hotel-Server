package com.vinova.booking_hotel.common.exception;

import com.vinova.booking_hotel.authentication.dto.response.ErrorDetail;
import com.vinova.booking_hotel.authentication.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(HttpServletRequest request, ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("NOTFOUND4041E");
        errorDetail.setErrorMessage("Resource not found");

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ErrorSignInException.class)
    public ResponseEntity<ErrorResponse> handleErrorSignInException(HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        
        ErrorDetail errorDetail = new ErrorDetail();
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
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("NOTMATCHPASSWORD");
        String errorMessage = "Password not match";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AccountIsBlockException.class)
    public ResponseEntity<ErrorResponse> handleAccountIsBlockException(HttpServletRequest request, AccountIsBlockException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("ACCOUNTISBLOCKED");
        errorDetail.setErrorMessage(ex.getMessage());

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(HttpServletRequest request, ResourceAlreadyExistsException ex) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("RESOURCEALREADYEXISTS");
        errorDetail.setErrorMessage("Resource already exists: " + ex.getResourceName() + " with " + ex.getFieldName());

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InValidVerifyEmailException.class)
    public ResponseEntity<ErrorResponse> handleInValidVerifyEmailException(HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();

        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("INVALIDVERIFYEMAIL");
        String errorMessage = "Invalid verify email";
        errorDetail.setErrorMessage(errorMessage);

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setErrorMessageId("VALIDATION_ERROR");
            errorDetail.setErrorMessage(fieldError.getDefaultMessage());
            errors.add(errorDetail);
        });

        errorResponse.setErrors(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidPageOrSizeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPageOrSizeException(HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        String errorKey = "INVALIDPAGEORSIZE";
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId(errorKey);
        errorDetail.setErrorMessage("Invalid page or size");
        
        errors.add(errorDetail);
        errorResponse.setErrors(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now().toString());
        errorResponse.setPath(request.getRequestURI());

        List<ErrorDetail> errors = new ArrayList<>();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorMessageId("UNKNOWN_ERROR");
        errorDetail.setErrorMessage(ex.getMessage());

        errors.add(errorDetail);
        errorResponse.setErrors(errors);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
}
