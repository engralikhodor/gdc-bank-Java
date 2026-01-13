package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class UsernameAlreadyUsedException extends RuntimeException
{
    public UsernameAlreadyUsedException()
    {
        super(AccountUtils.USERNAME_ALREADY_TAKEN);
    }
}
