package com.alikhdr.bankingApp.utils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Year;

public class AccountUtils
{

    private AccountUtils()
    {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String ACCOUNT_EXISTS_CODE = "001";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "User with provided email already exists!";
    public static final String PHONE_NUMBER_ALREADY_EXISTS_MESSAGE = "User with provided phone number already exists!";
    public static final String ALTERNATIVE_NUMBER_ALREADY_EXISTS_MESSAGE = "User with provided alternative phone number already exists!";
    public static final String GOVERNMENT_ID_ALREADY_EXISTS_MESSAGE = "User with provided government ID already exists!";
    public static final String ACCOUNT_CREATION_SUCCESS_CODE = "002";
    public static final String ACCOUNT_CREATION_SUCCESS_MESSAGE = "Account created successfully!";
    public static final String ACCOUNT_NOT_FOUND_CODE = "003";
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found!";
    public static final String ACCOUNT_FOUND_CODE = "004";
    public static final String ACCOUNT_FOUND_MESSAGE = "Account found!";
    public static final String ACCOUNT_CREDITED_SUCCESS_CODE = "005";
    public static final String ACCOUNT_CREDITED_SUCCESS_MESSAGE = "Account credited successfully!";
    public static final String ACCOUNT_DEBITED_SUCCESS_CODE = "006";
    public static final String ACCOUNT_DEBITED_SUCCESS_MESSAGE = "Account debited successfully!";
    public static final String INSUFFICIENT_BALANCE_CODE = "007";
    public static final String INSUFFICIENT_BALANCE_MESSAGE = "Insufficient balance!";
    public static final String EXCEEDS_TRANSFER_LIMIT_CODE = "008";
    public static final String EXCEEDS_TRANSFER_LIMIT_MESSAGE = "Exceeds transfer limit!";
    public static final String TRANSFER_SUCCESS_CODE = "009";
    public static final String TRANSFER_SUCCESS_MESSAGE = "Amount transferred successfully!";
    public static final String USER_FOUND_SUCCESS_CODE = "010";
    public static final String USER_FOUND_SUCCESS_MESSAGE = "User found successfully!";
    public static final String TRANSACTION_FOUND_SUCCESS_CODE = "011";
    public static final String TRANSACTION_FOUND_SUCCESS_MESSAGE = "Transaction found successfully!";
    public static final String USER_NOT_FOUND_SUCCESS_CODE = "012";
    public static final String USER_NOT_FOUND_SUCCESS_MESSAGE = "User not found!";
    public static final String TRANSACTION_NOT_FOUND_SUCCESS_CODE = "013";
    public static final String TRANSACTION_NOT_FOUND_SUCCESS_MESSAGE = "Transaction not found!";
    public static final String SAME_ACCOUNT_TRANSFER_CODE = "014";
    public static final String SAME_ACCOUNT_TRANSFER_MESSAGE = "You can't send from-to the same account!";
 

    public static final BigDecimal DEFAULT_TRANSFER_LIMIT = new BigDecimal("5000.00");

    /**
     * Generates a 10-digit account number: Current Year (4 digits) + Random (6 digits)
     */
    public static String generateAccountNumber()
    {
        Year currentYear = Year.now();
        // SecureRandom is better for finTech security
        SecureRandom random = new SecureRandom();
        int randomNumber = 100_000 + random.nextInt(900_000);

        return String.valueOf(currentYear) + randomNumber;
    }
}
