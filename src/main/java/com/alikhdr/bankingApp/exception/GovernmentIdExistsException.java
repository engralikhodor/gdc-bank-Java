package com.alikhdr.bankingApp.exception;

public class GovernmentIdExistsException extends RuntimeException
{
    public GovernmentIdExistsException(String message)
    {
        super(message);
    }
}
