package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants; // Import ResponseConstants

public class InvalidRefreshTokenException extends RuntimeException
{
    public InvalidRefreshTokenException()
    {
        super(ResponseConstants.INVALID_REFRESH_TOKEN); // Changed
    }
}
