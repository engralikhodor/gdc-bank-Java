package com.alikhdr.bankingApp.constants;

import java.math.BigDecimal;

public class ResponseConstants
{

    // Customer related messages and codes
    public static final String CUSTOMER_EMAIL_ALREADY_EXISTS = "Customer with provided email already exists!";
    public static final String CUSTOMER_PHONE_ALREADY_EXISTS = "Customer with provided phone number already exists!";
    public static final String CUSTOMER_ALTERNATIVE_PHONE_ALREADY_EXISTS = "Customer with provided alternative phone number already exists!";
    public static final String CUSTOMER_GOVERNMENT_ID_ALREADY_EXISTS = "Customer with provided government ID already exists!";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found!";
    public static final String CUSTOMER_NOT_FOUND_CODE = "001";
    public static final String CUSTOMER_FOUND = "Customer found.";
    public static final String CUSTOMER_FOUND_CODE = "002";

    // Transaction related messages and codes
    public static final String CUSTOMER_CREDITED_SUCCESSFULLY = "Customer credited successfully.";
    public static final String CUSTOMER_CREDITED_SUCCESSFULLY_CODE = "003";
    public static final String CUSTOMER_DEBITED_SUCCESSFULLY = "Customer debited successfully.";
    public static final String CUSTOMER_DEBITED_SUCCESSFULLY_CODE = "004";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance!";
    public static final String INSUFFICIENT_BALANCE_CODE = "005";
    public static final String EXCEEDS_TRANSFER_LIMIT = "Exceeds transfer limit!";
    public static final String EXCEEDS_TRANSFER_LIMIT_CODE = "006";
    public static final String TRANSFER_SUCCESSFUL_MESSAGE = "Amount transferred successfully.";
    public static final String TRANSFER_SUCCESSFUL_CODE = "007";
    public static final String SAME_ACCOUNT_TRANSFER = "You can't send from-to the same account!";
    public static final String SAME_ACCOUNT_TRANSFER_CODE = "008";

    // Authentication related messages and codes
    public static final String USERNAME_ALREADY_TAKEN = "Username already taken!";
    public static final String USERNAME_ALREADY_TAKEN_CODE = "009";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token!";
    public static final String INVALID_REFRESH_TOKEN_CODE = "010";

    // General Error Codes
    public static final String DUPLICATE_ENTRY_CODE = "011"; // For email, phone, government ID already exists
    public static final String VALIDATION_ERROR_CODE = "012"; // For MethodArgumentNotValidException
    public static final String OPTIMISTIC_LOCK_FAILURE_CODE = "013"; // For ObjectOptimisticLockingFailureException
    public static final String GENERIC_ERROR_CODE = "099"; // For generic Exception.class
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal server error"; // Generic message

    // Other constants
    public static final BigDecimal DEFAULT_TRANSFER_LIMIT = new BigDecimal("5000.00");

    private ResponseConstants()
    {
        // Private constructor to prevent instantiation
    }
}
