package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class EmailAlreadyExistsException extends RuntimeException
{
    public EmailAlreadyExistsException()
    {
        super(AccountUtils.CUSTOMER_EMAIL_ALREADY_EXISTS);
    }
}
