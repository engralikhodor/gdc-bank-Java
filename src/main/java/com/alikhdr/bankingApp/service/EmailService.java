package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.dto.EmailDetailsDTO;

public interface EmailService
{
    void sendEmailNotification(EmailDetailsDTO emailDetailsDTO);
}
