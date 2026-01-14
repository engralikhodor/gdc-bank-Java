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
    public ResponseEntity<GlobalResponse<?>> handleEmailExists(EmailAlreadyExistsException ex)
    {
        return new ResponseEntity<>(
                GlobalResponse.builder()
                        .responseCode("409")
                        .responseMessage(ex.getMessage())
                        .build(),
                HttpStatus.CONFLICT
        );
    }

    // Phone number already exists
    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    public ResponseEntity<GlobalResponse<?>> handlePhoneNumberExists(PhoneNumberAlreadyExistsException ex)
    {
        return new ResponseEntity<>(
                GlobalResponse.builder()
                        .responseCode("409")
                        .responseMessage(ex.getMessage())
                        .build(),
                HttpStatus.CONFLICT
        );
    }

    // Additional phone number exists
    @ExceptionHandler(AlternativePhoneNumberExistsException.class)
    public ResponseEntity<GlobalResponse<?>> handleAdditionalPhoneNumberExists(AlternativePhoneNumberExistsException ex)
    {
        return new ResponseEntity<>(
                GlobalResponse.builder()
                        .responseCode("409")
                        .responseMessage(ex.getMessage())
                        .build(),
                HttpStatus.CONFLICT
        );
    }

    // Government ID already exists
    @ExceptionHandler(GovernmentIdExistsException.class)
    public ResponseEntity<GlobalResponse<?>> handleGovernmentIdExists(GovernmentIdExistsException ex)
    {
        return new ResponseEntity<>(
                GlobalResponse.builder()
                        .responseCode("409")
                        .responseMessage(ex.getMessage())
                        .build(),
                HttpStatus.CONFLICT
        );
    }

    // Input validations
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<?>> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        String errorMessage = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(HttpStatus.BAD_REQUEST.toString())
                .responseMessage(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Optimistic Locking
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<GlobalResponse<?>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(HttpStatus.CONFLICT.toString())
                .responseMessage("This record was updated by another process. Please refresh and try again.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // account not found
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<GlobalResponse<?>> handleAccountNotFound(AccountNotFoundException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // sending from-to same account
    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<GlobalResponse<?>> handleSameAccountTransfer(SameAccountTransferException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(AccountUtils.SAME_ACCOUNT_TRANSFER_CODE)
                .responseMessage(AccountUtils.SAME_ACCOUNT_TRANSFER)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // amount exceeds limit
    @ExceptionHandler(ExceedsTransferLimitException.class)
    public ResponseEntity<GlobalResponse<?>> handleExceedsTransferLimit(ExceedsTransferLimitException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientResourcesException.class)
    public ResponseEntity<GlobalResponse<?>> handleInsufficientResources(InsufficientResourcesException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                .responseMessage(AccountUtils.INSUFFICIENT_BALANCE)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // username already taken
    @ExceptionHandler(UsernameAlreadyUsedException.class)
    public ResponseEntity<GlobalResponse<?>> handleUsernameAlreadyUsed(UsernameAlreadyUsedException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<AuthResponse>builder()
                .responseCode(AccountUtils.USERNAME_ALREADY_TAKEN_CODE)
                .responseMessage(AccountUtils.USERNAME_ALREADY_TAKEN)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // GENERIC
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<?>> handleGenericException(Exception ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode("500")
                .responseMessage("Internal server error")
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
