package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class InsufficientResourcesException extends RuntimeException
{
    public InsufficientResourcesException()
    {
        super(AccountUtils.INSUFFICIENT_BALANCE);
    }
}
