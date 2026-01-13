package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class ExceedsTransferLimitException extends RuntimeException
{
    public ExceedsTransferLimitException()
    {
        super(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE);
    }
}
