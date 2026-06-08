package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class ExceedsTransferLimitException extends RuntimeException
{
    public ExceedsTransferLimitException()
    {
        super(ResponseConstants.EXCEEDS_TRANSFER_LIMIT);
    }
}
