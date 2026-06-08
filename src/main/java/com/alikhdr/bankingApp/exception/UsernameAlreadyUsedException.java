package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants;

public class UsernameAlreadyUsedException extends RuntimeException
{
    public UsernameAlreadyUsedException()
    {
        super(ResponseConstants.USERNAME_ALREADY_TAKEN);
    }
}
