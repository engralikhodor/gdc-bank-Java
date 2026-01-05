package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.dto.ResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice // watching
public class GlobalExceptionHandler
{
    // Email already exists
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ResponseDTO> handleEmailExists(EmailAlreadyExistsException ex)
    {
        ResponseDTO response = ResponseDTO.builder()
                .responseCode("409")
                .responseMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Phone number already exists
    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<ResponseDTO> handlePhoneNumberExists(PhoneNumberAlreadyExistsException ex)
    {
        ResponseDTO response = ResponseDTO.builder()
                .responseCode("409")
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Additional phone number exists
    @ExceptionHandler(AlternativePhoneNumberExistsException.class)
    public ResponseEntity<ResponseDTO> handleAdditionalPhoneNumberExists(AlternativePhoneNumberExistsException ex)
    {
        ResponseDTO response = ResponseDTO.builder()
                .responseCode("409")
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Government ID already exists
    @ExceptionHandler(GovernmentIdExistsException.class)
    public ResponseEntity<ResponseDTO> handleGovernmentIdExists(GovernmentIdExistsException ex)
    {
        ResponseDTO response = ResponseDTO.builder()
                .responseCode("409")
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Input validations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ResponseDTO response = ResponseDTO.builder()
                .responseCode("400")
                .responseMessage(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
