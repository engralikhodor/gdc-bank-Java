package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class GovernmentIdExistsException extends RuntimeException
{
    public GovernmentIdExistsException()
    {
        super(ResponseConstants.CUSTOMER_GOVERNMENT_ID_ALREADY_EXISTS);
    }
}
