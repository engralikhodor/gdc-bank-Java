package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.EmailDetailsDTO;
import com.alikhdr.bankingApp.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@Slf4j
@RequiredArgsConstructor // Generates constructor for final fields
public class EmailServiceImpl implements EmailService
{
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmailAddress;

    @Value("${spring.application.bankName}")
    private String bankName;

    @Override
    @Async
    public void sendEmailAlert(EmailDetailsDTO emailDetailsDTO)
    {
        try
        {
            log.info("Starting background email task for: {}", emailDetailsDTO.recipient());
            //     SimpleMailMessage msg =  => old way
            MimeMessage msg = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
            helper.setFrom(senderEmailAddress, bankName);
            helper.setTo(emailDetailsDTO.recipient());
            helper.setSubject(emailDetailsDTO.subject());
            helper.setText(emailDetailsDTO.messageBody(), true);// 'true' = enables HTML rendering

            javaMailSender.send(msg);
            log.info("Email sent successfully to: {}", emailDetailsDTO.recipient());
        }
        catch (MessagingException | UnsupportedEncodingException e)
        {
            //            throw new RuntimeException(e);
            log.error("Failed to send HTML email", e);
        }
    }
}
