package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class AccountNotFoundException extends RuntimeException
{
    public AccountNotFoundException()
    {
        super(AccountUtils.CUSTOMER_NOT_FOUND);
    }
}
