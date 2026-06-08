package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants; // Import ResponseConstants

public class ExceedsTransferLimitException extends RuntimeException
{
    public ExceedsTransferLimitException()
    {
        super(ResponseConstants.EXCEEDS_TRANSFER_LIMIT); // Changed to message, not code
    }
}
