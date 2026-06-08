package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class AccountNotFoundException extends RuntimeException
{
    public AccountNotFoundException()
    {
        super(ResponseConstants.CUSTOMER_NOT_FOUND);
    }
}
