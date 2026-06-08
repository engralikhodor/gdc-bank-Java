package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class AlternativePhoneNumberExistsException extends RuntimeException
{
    public AlternativePhoneNumberExistsException()
    {
        super(ResponseConstants.CUSTOMER_ALTERNATIVE_PHONE_ALREADY_EXISTS);
    }
}
