package com.alikhdr.bankingApp.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException
{
    public PhoneNumberAlreadyExistsException(String message)
    {
        super(message);
    }
}
