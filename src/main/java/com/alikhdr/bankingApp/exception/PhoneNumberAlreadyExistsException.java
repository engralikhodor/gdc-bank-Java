package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class PhoneNumberAlreadyExistsException extends RuntimeException
{
    public PhoneNumberAlreadyExistsException()
    {
        super(AccountUtils.CUSTOMER_PHONE_ALREADY_EXISTS);
    }
}
