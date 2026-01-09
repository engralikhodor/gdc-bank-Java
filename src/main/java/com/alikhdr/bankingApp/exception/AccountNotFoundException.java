package com.alikhdr.bankingApp.exception;

public class AccountNotFoundException extends RuntimeException
{
    public AccountNotFoundException(String message)
    {
        super(message);
    }
}
