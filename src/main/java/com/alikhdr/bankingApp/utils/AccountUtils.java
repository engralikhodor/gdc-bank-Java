package com.alikhdr.bankingApp.utils;

import java.security.SecureRandom;
import java.time.Year;

public class AccountUtils
{
    private AccountUtils()
    {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a 10-digit account number: Current Year (4 digits) + Random (6 digits)
     * Uses SecureRandom for better security in financial contexts.
     * @return A unique 10-digit account number string.
     */
    public static String generateAccountNumber()
    {
        Year currentYear = Year.now();
        // SecureRandom is better for finTech security
        SecureRandom random = new SecureRandom();
        // Generate a 6-digit random number (100,000 to 999,999)
        int randomNumber = 100_000 + random.nextInt(900_000);

        return String.valueOf(currentYear) + randomNumber;
    }
}
