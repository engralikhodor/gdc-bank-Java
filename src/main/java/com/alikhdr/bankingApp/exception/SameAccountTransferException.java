package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class SameAccountTransferException extends RuntimeException
{
    public SameAccountTransferException()
    {
        super(ResponseConstants.SAME_ACCOUNT_TRANSFER);
    }
}
