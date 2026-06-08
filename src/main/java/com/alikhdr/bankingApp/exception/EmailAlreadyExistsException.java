package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class EmailAlreadyExistsException extends RuntimeException
{
    public EmailAlreadyExistsException()
    {
        super(ResponseConstants.CUSTOMER_EMAIL_ALREADY_EXISTS);
    }
}
