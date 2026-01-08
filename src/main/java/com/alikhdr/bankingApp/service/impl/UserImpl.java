package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.exception.AlternativePhoneNumberExistsException;
import com.alikhdr.bankingApp.exception.EmailAlreadyExistsException;
import com.alikhdr.bankingApp.exception.GovernmentIdExistsException;
import com.alikhdr.bankingApp.exception.PhoneNumberAlreadyExistsException;
import com.alikhdr.bankingApp.mapper.UserMapper;
import com.alikhdr.bankingApp.repository.UserRepository;
import com.alikhdr.bankingApp.service.EmailService;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.service.UserService;
import com.alikhdr.bankingApp.specs.UserSpecs;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor // Generates constructor for final fields
public class UserImpl implements UserService
{
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final TransactionService transactionService;

    @Value("${spring.application.bankName}")
    private String bankName;

    @Override
    public ApiResponse createAccount(UserRequest userRequest)
    {
        if (userRepository.existsByEmail(userRequest.getEmail()))
        {
            throw new EmailAlreadyExistsException(AccountUtils.EMAIL_ALREADY_EXISTS_MESSAGE);
        }

        if (userRepository.existsByPhoneNumber(userRequest.getPhoneNumber()))
        {
            throw new PhoneNumberAlreadyExistsException(AccountUtils.PHONE_NUMBER_ALREADY_EXISTS_MESSAGE);
        }

        if (userRepository.existsByGovernmentId(userRequest.getGovernmentId()))
        {
            throw new GovernmentIdExistsException(AccountUtils.GOVERNMENT_ID_ALREADY_EXISTS_MESSAGE);
        }

        // alternativePhoneNumber can be null
        if (userRequest.getAlternativePhoneNumber() != null && userRepository.existsByAlternativePhoneNumber(userRequest.getAlternativePhoneNumber()))
        {
            throw new AlternativePhoneNumberExistsException(AccountUtils.ALTERNATIVE_NUMBER_ALREADY_EXISTS_MESSAGE);
        }

        User newUser = userMapper.requestToEntity(userRequest); //MapStruct
        newUser.setAccountNumber(AccountUtils.generateAccountNumber());
        newUser.setAccountBalance(BigDecimal.ZERO);
        newUser.setDailyTransferLimit(AccountUtils.DEFAULT_TRANSFER_LIMIT);// Default bank policy
        newUser.setBaseCurrency(CurrencyOptions.USD);
        newUser.setStatus(AccountStatusOptions.ACTIVE);

        User savedUser = userRepository.save(newUser);

        String htmlBody = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                        <h2 style="color: #2c3e50;">Welcome to %s!</h2>
                        <p>Dear %s,</p>
                        <p>Your bank account has been successfully created. Here are your account details:</p>
                        <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Account Name:</strong> %s %s</p>
                            <p><strong>Account Number:</strong> <span style="color: #2980b9; font-size: 1.2em;">%s</span></p>
                            <p><strong>Status:</strong> ACTIVE</p>
                        </div>
                        <p>Please keep this information secure. You can now log in and start managing your finances.</p>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <footer style="font-size: 0.8em; color: #888;">
                            This is an automated message from %s Security Team.
                        </footer>
                    </div>
                """.formatted(
                bankName,
                savedUser.getFirstName(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getAccountNumber(),
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(savedUser.getEmail())
                .subject("Account Created Successfully")
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);// async

        UserResponse dto =
                UserResponse.builder()
                        .accountNumber(savedUser.getAccountNumber())
                        .accountBalance(savedUser.getAccountBalance())
                        .accountName(
                                String.join(" ",
                                        savedUser.getFirstName(),
                                        savedUser.getLastName(),
                                        savedUser.getOtherName()
                                ).trim()
                        )
                        .phoneNumber(savedUser.getPhoneNumber())
                        .build();

        return ApiResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .data(List.of(dto))
                .build();

    }

    public ApiResponse balanceEnquiry(EnquiryRequest enquiryRequest)
    {
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .data(null)
                    .build();
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        UserResponse dto = UserResponse.builder()
                .accountBalance(foundUser.getAccountBalance())
                .accountNumber(foundUser.getAccountNumber())
                .accountName(
                        String.join(" ",
                                foundUser.getFirstName(),
                                foundUser.getLastName(),
                                foundUser.getOtherName()
                        ).trim()
                )
                .phoneNumber(foundUser.getPhoneNumber())
                .build();

        return ApiResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .data(List.of(dto))
                .build();
    }

    public String nameEnquiry(EnquiryRequest enquiryRequest)
    {
        boolean isAccountExist = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!isAccountExist)
        {
            return AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE;
        }
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        return String.join(" ",
                foundUser.getFirstName(),
                foundUser.getLastName(),
                foundUser.getOtherName()
        ).trim();
    }

    // Helper (handle actual balance and transaction log)
    private void executeBalanceUpdate(
            String accountNumber,
            BigDecimal amount,
            TransactionTypeOptions type,
            String remarks)
    {
        User user = userRepository.findByAccountNumber(accountNumber);

        if (type == TransactionTypeOptions.CREDIT)
        {
            user.setAccountBalance(user.getAccountBalance().add(amount));
        }
        else
        {
            user.setAccountBalance(user.getAccountBalance().subtract(amount));
        }

        userRepository.save(user);

        // Save history without triggering an email
        transactionService.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .accountNumber(accountNumber)
                .transactionType(type)
                .status("COMPLETED")
                .remarks(remarks)
                .build());
    }

    @Transactional // non-negotiable in finTech. If the system crashes mid-save, it rolls back
    public ApiResponse creditAccount(CreditDebitRequest creditDebitRequest)
    {
        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .data(null)
                    .build();
        }
        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        BigDecimal amountToBeCredited = creditDebitRequest.getAmount();
        if (amountToBeCredited.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                    .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE)
                    .data(null)
                    .build();
        }

        BigDecimal newBalance = userToCredit.getAccountBalance().add(creditDebitRequest.getAmount());

        userToCredit.setAccountBalance(newBalance);
        // Save in user table
        userRepository.save(userToCredit);

        // Save in transaction table
        transactionService.saveTransaction(TransactionRequest.builder()
                .amount(amountToBeCredited)
                .accountNumber(userToCredit.getAccountNumber())
                .transactionType(TransactionTypeOptions.CREDIT)
                .build());

        String htmlBody = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                        <h2 style="color: #2c3e50;">Welcome to %s!</h2>
                        <p>Dear %s,</p>
                        <p>Your bank account has been credited with the amount: %s and your new balance is: %s</p>
                        <p>Please keep this information secure. You can now log in and start managing your finances.</p>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <footer style="font-size: 0.8em; color: #888;">
                            This is an automated message from %s Security Team.
                        </footer>
                    </div>
                """.formatted(
                bankName,
                userToCredit.getFirstName(),
                amountToBeCredited,
                newBalance,
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(userToCredit.getEmail())
                .subject("Account Credited ")
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);// async

        UserResponse dto = UserResponse.builder()
                .accountName(
                        String.join(" ",
                                userToCredit.getFirstName(),
                                userToCredit.getLastName(),
                                userToCredit.getOtherName()
                        ).trim()
                )
                .accountNumber(userToCredit.getAccountNumber())
                .accountBalance(newBalance)
                .phoneNumber(userToCredit.getPhoneNumber())
                .build();

        return ApiResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .data(List.of(dto))
                .build();

    }

    @Transactional // non-negotiable in finTech. If the system crashes mid-save, it rolls back
    public ApiResponse debitAccount(CreditDebitRequest creditDebitRequest)
    {
        boolean isAccountExist = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if (!isAccountExist)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .data(null)
                    .build();

        }

        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        BigDecimal amountToBeDebited = creditDebitRequest.getAmount();
        if (amountToBeDebited.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                    .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE)
                    .data(null)
                    .build();
        }

        BigDecimal currentBalance = userToDebit.getAccountBalance();
        if (currentBalance.compareTo(amountToBeDebited) < 0)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .data(null)
                    .build();
        }

        BigDecimal newBalance = userToDebit.getAccountBalance().subtract(creditDebitRequest.getAmount());
        userToDebit.setAccountBalance(newBalance);
        // Save in user table
        userRepository.save(userToDebit);

        // Save in transaction table
        transactionService.saveTransaction(TransactionRequest.builder()
                .amount(amountToBeDebited)
                .accountNumber(userToDebit.getAccountNumber())
                .transactionType(TransactionTypeOptions.DEBIT)
                .build());

        String htmlBody = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                        <h2 style="color: #2c3e50;">Welcome to %s!</h2>
                        <p>Dear %s,</p>
                        <p>Your bank account has been debited with the amount: %s and your new balance is: %s</p>
                        <p>Please keep this information secure. You can now log in and start managing your finances.</p>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <footer style="font-size: 0.8em; color: #888;">
                            This is an automated message from %s Security Team.
                        </footer>
                    </div>
                """.formatted(
                bankName,
                userToDebit.getFirstName(),
                amountToBeDebited,
                newBalance,
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(userToDebit.getEmail())
                .subject("Account Debited")
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);// async

        UserResponse dto = UserResponse.builder()
                .accountName(
                        String.join(" ",
                                userToDebit.getFirstName(),
                                userToDebit.getLastName(),
                                userToDebit.getOtherName()
                        ).trim()
                )
                .accountNumber(userToDebit.getAccountNumber())
                .accountBalance(newBalance)
                .phoneNumber(userToDebit.getPhoneNumber())
                .build();

        return ApiResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                .data(List.of(dto))
                .build();
    }

    @Override
    @Transactional // non-negotiable in finTech. If the system crashes mid-save, it rolls back
    public ApiResponse transferAmount(TransferRequest transferRequest)
    {
        // Check if both accounts exist
        User fromUser = userRepository.findByAccountNumber(transferRequest.getFromAccountNumber());
        User toUser = userRepository.findByAccountNumber(transferRequest.getToAccountNumber());

        if (fromUser == null || toUser == null)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .build();
        }

        BigDecimal amount = transferRequest.getAmountToTransfer();
        String remarks = transferRequest.getRemarks();

        // Stop user from sending amount > transfer limit
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                    .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE)
                    .build();
        }

        // Stop user from sending amount > current balance
        if (fromUser.getAccountBalance().compareTo(amount) < 0)
        {
            return ApiResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .build();
        }

        // 1 transfer action = 1 debit (from) + 1 credit (to)
        executeBalanceUpdate(fromUser.getAccountNumber(), amount, TransactionTypeOptions.DEBIT, remarks);
        executeBalanceUpdate(toUser.getAccountNumber(), amount, TransactionTypeOptions.CREDIT, remarks);

        // Email the sender (from)
        String senderHtml = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee;">
                        <h2 style="color: #c0392b;">Debit Alert</h2>
                        <p>Dear %s,</p>
                        <p>You have successfully transferred <strong>%s %s</strong> to <strong>%s</strong>.</p>
                        <p>Your new balance is: <strong>%s</strong></p>
                    </div>
                """.formatted(
                fromUser.getFirstName(),
                fromUser.getBaseCurrency(),
                amount,
                String.join(" ",
                        toUser.getFirstName(),
                        toUser.getLastName(),
                        toUser.getOtherName()
                ).trim(),
                fromUser.getAccountBalance().subtract(amount));

        emailService.sendEmailNotification(EmailDetailsDTO.builder()
                .recipient(fromUser.getEmail())
                .subject("Transfer Sent Confirmation")
                .messageBody(senderHtml)
                .build());

        // Email the receiver (to)
        String receiverHtml = """
                    <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee;">
                        <h2 style="color: #27ae60;">Credit Alert</h2>
                        <p>Dear %s,</p>
                        <p>Your account has been credited with <strong>%s %s</strong> sent by <strong>%s</strong>.</p>
                        <p>Your new balance is: <strong>%s</strong></p>
                    </div>
                """.formatted(toUser.getFirstName(), toUser.getBaseCurrency(), amount,
                String.join(" ",
                        fromUser.getFirstName(),
                        fromUser.getLastName(),
                        fromUser.getOtherName()
                ).trim(),
                toUser.getAccountBalance().add(amount));

        emailService.sendEmailNotification(EmailDetailsDTO.builder()
                .recipient(toUser.getEmail())
                .subject("Money Received!")
                .messageBody(receiverHtml)
                .build());

        log.info("Transfer successful: {} moved from {} to {}", amount, fromUser.getAccountNumber(), toUser.getAccountNumber());

        return ApiResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .build();
    }

    @Override
    public List<UserResponse>
    searchUsers(UserSearchCriteria userSearchCriteria)
    {
        Specification<User> spec = Specification
                .where(UserSpecs.isEquals(User_.EMAIL, userSearchCriteria.getEmail()))
                .and(UserSpecs.isEquals(User_.ACCOUNT_NUMBER, userSearchCriteria.getAccountNumber()))
                .and(UserSpecs.isAbove(userSearchCriteria.getMinAge()));

        return userRepository.findAll(spec)
                .stream()
                .map(userMapper::entityToResponse)
                .collect(Collectors.toList());
    }
}
