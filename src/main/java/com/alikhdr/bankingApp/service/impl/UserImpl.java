package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.exception.*;
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
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserImpl implements UserService
{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final TransactionService transactionService;

    @Value("${spring.application.bankName}")
    private String bankName;

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest)
    {
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        if (foundUser == null)
        {
            return AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE;
        }

        return String.join(" ",
                foundUser.getFirstName(),
                foundUser.getLastName(),
                foundUser.getOtherName()
        ).trim();
    }


    private void sendTransactionEmail(User user, BigDecimal amount, String transactionType)
    {
        String htmlBody = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                        <h2 style="color: #2c3e50;">Transaction Alert: %s</h2>
                        <p>Dear %s,</p>
                        <p>Your account has been <strong>%s</strong> with the amount: <strong>%s %s</strong>.</p>
                        <p>Your new available balance is: <strong>%s</strong></p>
                        <hr style="border: 0; border-top: 1px solid #eee;" />
                        <footer style="font-size: 0.8em; color: #888;">
                            This is an automated security notification from %s.
                        </footer>
                    </div>
                """.formatted(
                transactionType,
                user.getFirstName(),
                transactionType.toLowerCase(),
                user.getBaseCurrency(),
                amount,
                user.getAccountBalance(),
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(user.getEmail())
                .subject("Account " + transactionType)
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);
    }

    @Override
    @Transactional // Atomic operation: either both accounts update or none do
    public ApiResponse<UserResponse> transferAmount(TransferRequest transferRequest)
    {
        User fromUser = userRepository.findByAccountNumber(transferRequest.getFromAccountNumber());
        User toUser = userRepository.findByAccountNumber(transferRequest.getToAccountNumber());

        // validate accounts
        if (fromUser == null || toUser == null)
        {
            throw new AccountNotFoundException(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE);
        }

        // prevent send from-to the same user
        if (Objects.equals(
                fromUser.getAccountNumber(),
                toUser.getAccountNumber()))
        {
            throw new SameAccountTransferException(AccountUtils.SAME_ACCOUNT_TRANSFER_MESSAGE);
        }

        BigDecimal amount = transferRequest.getAmountToTransfer();

        // validate balance and limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE);
        }

        if (fromUser.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE);
        }

        // execute updates via helper to ensure transaction logs are created
        executeBalanceUpdate(fromUser.getAccountNumber(), amount, TransactionTypeOptions.DEBIT, transferRequest.getRemarks());
        executeBalanceUpdate(toUser.getAccountNumber(), amount, TransactionTypeOptions.CREDIT, transferRequest.getRemarks());

        log.info("Transfer successful: {} moved from {} to {}", amount, fromUser.getAccountNumber(), toUser.getAccountNumber());

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .data(userMapper.entityToResponse(fromUser)) // Return sender's updated status
                .build();
    }

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

    @Override
    @Transactional
    public ApiResponse<UserResponse> createAccount(UserRequest userRequest)
    {
        if (userRepository.existsByEmail(userRequest.getEmail()))
        {
            throw new EmailAlreadyExistsException(AccountUtils.EMAIL_ALREADY_EXISTS_MESSAGE);
        }

        if (userRepository.existsByPhoneNumber(userRequest.getEmail()))
        {
            throw new PhoneNumberAlreadyExistsException(AccountUtils.PHONE_NUMBER_ALREADY_EXISTS_MESSAGE);
        }

        if (userRequest.getAlternativePhoneNumber() != null
                && userRepository.existsByAlternativePhoneNumber((userRequest.getAlternativePhoneNumber())))
        {
            throw new AlternativePhoneNumberExistsException(AccountUtils.ALTERNATIVE_NUMBER_ALREADY_EXISTS_MESSAGE);
        }

        if (userRepository.existsByAlternativePhoneNumber((userRequest.getGovernmentId())))
        {
            throw new GovernmentIdExistsException(AccountUtils.GOVERNMENT_ID_ALREADY_EXISTS_MESSAGE);
        }

        User newUser = userMapper.requestToEntity(userRequest);
        newUser.setAccountNumber(AccountUtils.generateAccountNumber());
        newUser.setAccountBalance(BigDecimal.ZERO);
        newUser.setDailyTransferLimit(AccountUtils.DEFAULT_TRANSFER_LIMIT);
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

        UserResponse data = userMapper.entityToResponse(savedUser);

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .data(data)
                .build();
    }

    @Override
    public ApiResponse<UserResponse> balanceEnquiry(EnquiryRequest enquiryRequest)
    {
        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        if (foundUser == null)
        {
            throw new AccountNotFoundException(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE);
        }

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .data(userMapper.entityToResponse(foundUser))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse>
    debitAccount(CreditDebitRequest creditDebitRequest)
    {
        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        // check if account exists
        if (userToDebit == null)
        {
            throw new AccountNotFoundException(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE);
        }

        BigDecimal amount = creditDebitRequest.getAmount();

        // validate against transfer limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE);
        }

        // check for sufficient funds
        if (userToDebit.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE);
        }

        // update balance and save
        userToDebit.setAccountBalance(userToDebit.getAccountBalance().subtract(amount));
        userRepository.save(userToDebit);

        // log transaction async.
        transactionService.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .accountNumber(userToDebit.getAccountNumber())
                .transactionType(TransactionTypeOptions.DEBIT)
                .status("COMPLETED")
                .remarks("Manual Debit")
                .build());

        // send notification (Async)
        sendTransactionEmail(userToDebit, amount, "Debited");

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                .data(userMapper.entityToResponse(userToDebit))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse>
    creditAccount(CreditDebitRequest request)
    {
        User userToCredit = userRepository.findByAccountNumber(request.getAccountNumber());

        if (userToCredit == null)
        {
            throw new AccountNotFoundException(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE);
        }

        BigDecimal amount = request.getAmount();

        // validate against transfer limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE);
        }

        userToCredit.setAccountBalance(userToCredit.getAccountBalance().add(request.getAmount()));
        userRepository.save(userToCredit);

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .data(userMapper.entityToResponse(userToCredit))
                .build();
    }

    @Override
    public List<UserResponse> searchUsers(UserSearchCriteria criteria)
    {
        Specification<User> spec = Specification
                .where(UserSpecs.isEquals(User_.EMAIL, criteria.getEmail()))
                .and(UserSpecs.isEquals(User_.ACCOUNT_NUMBER, criteria.getAccountNumber()))
                .and(UserSpecs.isAbove(criteria.getMinAge()));

        return userRepository.findAll(spec)
                .stream()
                .map(userMapper::entityToResponse)
                .collect(Collectors.toList());
    }
}
