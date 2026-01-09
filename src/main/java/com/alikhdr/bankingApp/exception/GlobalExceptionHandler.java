package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.dto.ApiResponse;
import com.alikhdr.bankingApp.dto.UserResponse;
import com.alikhdr.bankingApp.utils.AccountUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.InsufficientResourcesException;
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

    @ExceptionHandler(AccountNotFoundException.class)
    public ApiResponse<?> handleAccountNotFound(AccountNotFoundException ex)
    {
        return ApiResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                .build();
    }

    @ExceptionHandler(SameAccountTransferException.class)
    public ApiResponse<?> handleSameAccountTransfer(SameAccountTransferException ex)
    {
        return ApiResponse.builder()
                .responseCode(AccountUtils.SAME_ACCOUNT_TRANSFER_CODE)
                .responseMessage(AccountUtils.SAME_ACCOUNT_TRANSFER_MESSAGE)
                .build();
    }

    @ExceptionHandler(ExceedsTransferLimitException.class)
    public ApiResponse<?> handleExceedsTransferLimit(ExceedsTransferLimitException ex)
    {
        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE)
                .build();
    }

    @ExceptionHandler(InsufficientResourcesException.class)
    public ApiResponse<?> handleInsufficientResources(InsufficientResourcesException ex)
    {
        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                .build();
    }

    // GENERIC
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleGenericException(Exception ex)
    {
        return ApiResponse.builder()
                .responseCode("500")
                .responseMessage("Internal server error")
                .build();
    }

}
