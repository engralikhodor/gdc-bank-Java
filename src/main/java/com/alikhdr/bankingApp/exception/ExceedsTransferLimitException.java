package com.alikhdr.bankingApp.exception;

public class ExceedsTransferLimitException extends RuntimeException
{
    public ExceedsTransferLimitException(String message)
    {
        super(message);
    }
}
