package com.alikhdr.bankingApp.exception;

public class EmailAlreadyExistsException extends RuntimeException
{
    public EmailAlreadyExistsException(String message)
    {
        super(message);
    }
}
