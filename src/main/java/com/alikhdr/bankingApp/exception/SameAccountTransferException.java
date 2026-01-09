package com.alikhdr.bankingApp.exception;

public class SameAccountTransferException extends RuntimeException
{
    public SameAccountTransferException(String message)
    {
        super(message);
    }
}
