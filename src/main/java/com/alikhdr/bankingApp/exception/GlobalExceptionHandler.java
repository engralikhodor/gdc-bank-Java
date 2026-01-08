package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice // watching
public class GlobalExceptionHandler
{
    // Email already exists
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleEmailExists(EmailAlreadyExistsException ex)
    {
        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Phone number already exists
    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handlePhoneNumberExists(PhoneNumberAlreadyExistsException ex)
    {
        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Additional phone number exists
    @ExceptionHandler(AlternativePhoneNumberExistsException.class)
    public ResponseEntity<ApiResponse> handleAdditionalPhoneNumberExists(AlternativePhoneNumberExistsException ex)
    {
        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Government ID already exists
    @ExceptionHandler(GovernmentIdExistsException.class)
    public ResponseEntity<ApiResponse> handleGovernmentIdExists(GovernmentIdExistsException ex)
    {
        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Input validations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.toString())
                .responseMessage(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Optimistic Locking
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex)
    {
        ApiResponse response = ApiResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage("This record was updated by another process. Please refresh and try again.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}
