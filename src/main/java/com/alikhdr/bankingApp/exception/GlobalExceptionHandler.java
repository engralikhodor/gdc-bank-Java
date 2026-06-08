package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.CustomerResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;
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
                        .responseCode(ResponseConstants.DUPLICATE_ENTRY_CODE) // Changed to custom code
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
                        .responseCode(ResponseConstants.DUPLICATE_ENTRY_CODE) // Changed to custom code
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
                        .responseCode(ResponseConstants.DUPLICATE_ENTRY_CODE) // Changed to custom code
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
                        .responseCode(ResponseConstants.DUPLICATE_ENTRY_CODE) // Changed to custom code
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
                .responseCode(ResponseConstants.VALIDATION_ERROR_CODE) // Changed to custom code
                .responseMessage(errorMessage)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Optimistic Locking
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<GlobalResponse<?>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(ResponseConstants.OPTIMISTIC_LOCK_FAILURE_CODE) // Changed to custom code
                .responseMessage("This record was updated by another process. Please refresh and try again.")
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // account not found (already uses custom code)
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<GlobalResponse<?>> handleAccountNotFound(AccountNotFoundException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(ResponseConstants.CUSTOMER_NOT_FOUND_CODE)
                .responseMessage(ResponseConstants.CUSTOMER_NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // sending from-to same account (already uses custom code)
    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<GlobalResponse<?>> handleSameAccountTransfer(SameAccountTransferException ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(ResponseConstants.SAME_ACCOUNT_TRANSFER_CODE)
                .responseMessage(ResponseConstants.SAME_ACCOUNT_TRANSFER)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // amount exceeds limit (already uses custom code)
    @ExceptionHandler(ExceedsTransferLimitException.class)
    public ResponseEntity<GlobalResponse<?>> handleExceedsTransferLimit(ExceedsTransferLimitException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<CustomerResponse>builder()
                .responseCode(ResponseConstants.EXCEEDS_TRANSFER_LIMIT_CODE)
                .responseMessage(ResponseConstants.EXCEEDS_TRANSFER_LIMIT)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientResourcesException.class)
    public ResponseEntity<GlobalResponse<?>> handleInsufficientResources(InsufficientResourcesException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<CustomerResponse>builder()
                .responseCode(ResponseConstants.INSUFFICIENT_BALANCE_CODE)
                .responseMessage(ResponseConstants.INSUFFICIENT_BALANCE)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // username already taken (already uses custom code)
    @ExceptionHandler(UsernameAlreadyUsedException.class)
    public ResponseEntity<GlobalResponse<?>> handleUsernameAlreadyUsed(UsernameAlreadyUsedException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<AuthResponse>builder()
                .responseCode(ResponseConstants.USERNAME_ALREADY_TAKEN_CODE)
                .responseMessage(ResponseConstants.USERNAME_ALREADY_TAKEN)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // Invalid refresh token (already uses custom code)
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<GlobalResponse<?>> handleInvalidRefreshToken(InvalidRefreshTokenException ex)
    {
        GlobalResponse<?> response = GlobalResponse.<AuthResponse>builder()
                .responseCode(ResponseConstants.INVALID_REFRESH_TOKEN_CODE)
                .responseMessage(ResponseConstants.INVALID_REFRESH_TOKEN)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // GENERIC
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<?>> handleGenericException(Exception ex)
    {
        GlobalResponse<?> response = GlobalResponse.builder()
                .responseCode(ResponseConstants.GENERIC_ERROR_CODE) // Changed to custom code
                .responseMessage(ResponseConstants.INTERNAL_SERVER_ERROR_MESSAGE) // Changed to custom message
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // Changed HTTP status to 500
    }
}
