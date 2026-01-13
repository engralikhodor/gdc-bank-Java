package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.CustomerResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;
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
    public ResponseEntity<GlobalResponse> handleEmailExists(EmailAlreadyExistsException ex)
    {
        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Phone number already exists
    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<GlobalResponse> handlePhoneNumberExists(PhoneNumberAlreadyExistsException ex)
    {
        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Additional phone number exists
    @ExceptionHandler(AlternativePhoneNumberExistsException.class)
    public ResponseEntity<GlobalResponse> handleAdditionalPhoneNumberExists(AlternativePhoneNumberExistsException ex)
    {
        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Government ID already exists
    @ExceptionHandler(GovernmentIdExistsException.class)
    public ResponseEntity<GlobalResponse> handleGovernmentIdExists(GovernmentIdExistsException ex)
    {
        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Input validations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.toString())
                .responseMessage(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Optimistic Locking
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<GlobalResponse> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex)
    {
        GlobalResponse response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage("This record was updated by another process. Please refresh and try again.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public GlobalResponse<?> handleAccountNotFound(AccountNotFoundException ex)
    {
        return GlobalResponse.builder()
                .responseCode(AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_NOT_FOUND)
                .build();
    }

    @ExceptionHandler(SameAccountTransferException.class)
    public GlobalResponse<?> handleSameAccountTransfer(SameAccountTransferException ex)
    {
        return GlobalResponse.builder()
                .responseCode(AccountUtils.SAME_ACCOUNT_TRANSFER_CODE)
                .responseMessage(AccountUtils.SAME_ACCOUNT_TRANSFER)
                .build();
    }

    @ExceptionHandler(ExceedsTransferLimitException.class)
    public GlobalResponse<?> handleExceedsTransferLimit(ExceedsTransferLimitException ex)
    {
        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT)
                .build();
    }

    @ExceptionHandler(InsufficientResourcesException.class)
    public GlobalResponse<?> handleInsufficientResources(InsufficientResourcesException ex)
    {
        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                .responseMessage(AccountUtils.INSUFFICIENT_BALANCE)
                .build();
    }

    @ExceptionHandler(UsernameAlreadyUsedException.class)
    public GlobalResponse<?> handleUsernameAlreadyUsed(UsernameAlreadyUsedException ex)
    {
        return GlobalResponse.<AuthResponse>builder()
                .responseCode(AccountUtils.USERNAME_ALREADY_TAKEN_CODE)
                .responseMessage(AccountUtils.USERNAME_ALREADY_TAKEN)
                .build();
    }

    // GENERIC
    @ExceptionHandler(Exception.class)
    public GlobalResponse<?> handleGenericException(Exception ex)
    {
        return GlobalResponse.builder()
                .responseCode("500")
                .responseMessage("Internal server error")
                .build();
    }
}
