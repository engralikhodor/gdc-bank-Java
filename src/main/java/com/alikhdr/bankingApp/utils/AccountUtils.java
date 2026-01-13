package com.alikhdr.bankingApp.utils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Year;

public class AccountUtils
{

    private AccountUtils()
    {
    }

    public static final String ACCOUNT_EXISTS_CODE = "001";

    public static final String CUSTOMER_EMAIL_ALREADY_EXISTS
            = "Customer with provided email already exists!";

    public static final String CUSTOMER_PHONE_ALREADY_EXISTS
            = "Customer with provided phone number already exists!";

    public static final String CUSTOMER_ALTERNATIVE_PHONE_ALREADY_EXISTS
            = "Customer with provided alternative phone number already exists!";

    public static final String CUSTOMER_GOVERNMENT_ID_ALREADY_EXISTS
            = "Customer with provided government ID already exists!";

    public static final String CUSTOMER_CREATED_SUCCESSFULLY = "Customer added successfully.";
    public static final String CUSTOMER_CREATED_SUCCESSFULLY_CODE = "002";

    public static final String CUSTOMER_NOT_FOUND = "Customer not found!";
    public static final String CUSTOMER_NOT_FOUND_CODE = "003";

    public static final String CUSTOMER_FOUND = "Customer found.";
    public static final String CUSTOMER_FOUND_CODE = "004";

    public static final String CUSTOMER_CREDITED_SUCCESSFULLY = "Customer credited successfully.";
    public static final String CUSTOMER_CREDITED_SUCCESSFULLY_CODE = "005";

    public static final String CUSTOMER_DEBITED_SUCCESSFULLY = "Customer debited successfully.";
    public static final String CUSTOMER_DEBITED_SUCCESSFULLY_CODE = "006";

    public static final String INSUFFICIENT_BALANCE = "Insufficient balance!";
    public static final String INSUFFICIENT_BALANCE_CODE = "007";

    public static final String EXCEEDS_TRANSFER_LIMIT = "Exceeds transfer limit!";
    public static final String EXCEEDS_TRANSFER_LIMIT_CODE = "008";

    public static final String AMOUNT_TRANSFERRED_SUCCESSFULLY = "Amount transferred successfully.";
    public static final String AMOUNT_TRANSFERRED_CODE = "009";

    public static final String TRANSACTION_FOUND_SUCCESSFULLY = "Transaction found successfully.";
    public static final String TRANSACTION_FOUND_SUCCESSFULLY_CODE = "010";

    public static final String TRANSACTION_NOT_FOUND = "Transaction not found!";
    public static final String TRANSACTION_NOT_FOUND_CODE = "011";

    public static final String SAME_ACCOUNT_TRANSFER = "You can't send from-to the same account!";
    public static final String SAME_ACCOUNT_TRANSFER_CODE = "012";

    public static final String USERNAME_ALREADY_TAKEN = "Username already taken!";
    public static final String USERNAME_ALREADY_TAKEN_CODE = "013";

    public static final String USERNAME_CREATED_SUCCESSFULLY = "Username created successfully.";
    public static final String USERNAME_CREATED_SUCCESSFULLY_CODE = "014";

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
