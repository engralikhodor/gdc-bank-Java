package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.utils.AccountUtils;

public class InvalidRefreshTokenException extends RuntimeException
{
    public InvalidRefreshTokenException()
    {
        super(AccountUtils.INVALID_REFRESH_TOKEN);
    }
}
