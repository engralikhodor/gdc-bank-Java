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

    @Override
    @Transactional // Non-negotiable for data integrity in financial operations
    public ApiResponse<UserResponse> debitAccount(CreditDebitRequest creditDebitRequest)
    {
        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        // check if account exists
        if (userToDebit == null)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .build();
        }

        BigDecimal amount = creditDebitRequest.getAmount();

        // validate against transfer limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                    .responseMessage(AccountUtils.EXCEEDS_TRANSFER_LIMIT_MESSAGE)
                    .build();
        }

        // check for sufficient funds
        if (userToDebit.getAccountBalance().compareTo(amount) < 0)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .build();
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
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .build();
        }

        BigDecimal amount = transferRequest.getAmountToTransfer();

        // validate balance and limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.EXCEEDS_TRANSFER_LIMIT_CODE)
                    .build();
        }

        if (fromUser.getAccountBalance().compareTo(amount) < 0)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .build();
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

        if (
                userRequest.getAlternativePhoneNumber() != null &&
                        userRepository.existsByAlternativePhoneNumber((userRequest.getAlternativePhoneNumber())))
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

        // Email logic...

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
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_FOUND_MESSAGE)
                    .build();
        }

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .data(userMapper.entityToResponse(foundUser))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<UserResponse> creditAccount(CreditDebitRequest request)
    {
        User user = userRepository.findByAccountNumber(request.getAccountNumber());
        if (user == null)
        {
            return ApiResponse.<UserResponse>builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_FOUND_CODE)
                    .build();
        }

        // Logic to update balance and save transaction...
        user.setAccountBalance(user.getAccountBalance().add(request.getAmount()));
        userRepository.save(user);

        return ApiResponse.<UserResponse>builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .data(userMapper.entityToResponse(user))
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
