package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants; // Import ResponseConstants

public class PhoneNumberAlreadyExistsException extends RuntimeException
{
    public PhoneNumberAlreadyExistsException()
    {
        super(ResponseConstants.CUSTOMER_PHONE_ALREADY_EXISTS); // Changed
    }
}
