package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class SameAccountTransferException extends RuntimeException
{
    public SameAccountTransferException()
    {
        super(AccountUtils.SAME_ACCOUNT_TRANSFER);
    }
}
