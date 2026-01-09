package com.alikhdr.bankingApp.exception;

public class InsufficientResourcesException extends RuntimeException
{
    public InsufficientResourcesException(String message)
    {
        super(message);
    }
}
