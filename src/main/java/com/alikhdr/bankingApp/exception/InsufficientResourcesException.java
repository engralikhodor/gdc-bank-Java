package com.alikhdr.bankingApp.exception;

import com.alikhdr.bankingApp.constants.ResponseConstants; // Import ResponseConstants

public class InsufficientResourcesException extends RuntimeException
{
    public InsufficientResourcesException()
    {
        super(ResponseConstants.INSUFFICIENT_BALANCE); // Changed
    }
}
