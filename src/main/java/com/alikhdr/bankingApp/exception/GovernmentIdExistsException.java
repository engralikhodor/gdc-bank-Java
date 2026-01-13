package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class GovernmentIdExistsException extends RuntimeException
{
    public GovernmentIdExistsException()
    {
        super(AccountUtils.CUSTOMER_GOVERNMENT_ID_ALREADY_EXISTS);
    }
}
