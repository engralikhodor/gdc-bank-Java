package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class AlternativePhoneNumberExistsException extends RuntimeException
{
    public AlternativePhoneNumberExistsException()
    {
        super(AccountUtils.CUSTOMER_ALTERNATIVE_PHONE_ALREADY_EXISTS);
    }
}
