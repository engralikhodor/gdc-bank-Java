package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.exception.*;
import com.alikhdr.bankingApp.mapper.CustomerMapper;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.CustomerService;
import com.alikhdr.bankingApp.service.EmailService;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.specs.CustomerSpecs;
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
public class CustomerImpl implements CustomerService
{

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final EmailService emailService;
    private final TransactionService transactionService;

    @Value("${spring.application.bankName}")
    private String bankName;

    @Override
    public GlobalResponse<String>
    nameEnquiry(EnquiryRequest enquiryRequest)
    {
        boolean exists = customerRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if (!exists)
        {
            return GlobalResponse.<String>builder()
                    .responseCode(AccountUtils.CUSTOMER_NOT_FOUND_CODE)
                    .responseMessage(AccountUtils.CUSTOMER_NOT_FOUND)
                    .data(null)
                    .build();
        }

        Customer foundCustomer = customerRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        String fullName = String.join(" ",
                foundCustomer.getFirstName(),
                foundCustomer.getLastName(),
                foundCustomer.getOtherName()
        ).trim();

        return GlobalResponse.<String>builder()
                .responseCode(AccountUtils.CUSTOMER_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_FOUND)
                .data(fullName) // Here T is a String
                .build();
    }

    private void
    sendTransactionEmail(Customer customer, BigDecimal amount, String transactionType)
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
                customer.getFirstName(),
                transactionType.toLowerCase(),
                customer.getBaseCurrency(),
                amount,
                customer.getAccountBalance(),
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(customer.getEmail())
                .subject("Account " + transactionType)
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);
    }

    @Override
    @Transactional // Atomic operation: either both accounts update or none do
    public GlobalResponse<CustomerResponse>
    transferAmount(TransferRequest transferRequest)
    {
        Customer fromCustomer = customerRepository.findByAccountNumber(transferRequest.getFromAccountNumber());
        Customer toCustomer = customerRepository.findByAccountNumber(transferRequest.getToAccountNumber());

        // validate accounts
        if (fromCustomer == null || toCustomer == null)
        {
            throw new AccountNotFoundException();
        }

        // prevent send from-to the same user
        if (Objects.equals(
                fromCustomer.getAccountNumber(),
                toCustomer.getAccountNumber()))
        {
            throw new SameAccountTransferException();
        }

        BigDecimal amount = transferRequest.getAmountToTransfer();

        // validate balance and limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        if (fromCustomer.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        // execute updates via helper to ensure transaction logs are created
        executeBalanceUpdate(fromCustomer.getAccountNumber(), amount, TransactionTypeOptions.DEBIT, transferRequest.getRemarks());
        executeBalanceUpdate(toCustomer.getAccountNumber(), amount, TransactionTypeOptions.CREDIT, transferRequest.getRemarks());

        log.info("Transfer successful: {} moved from {} to {}", amount, fromCustomer.getAccountNumber(), toCustomer.getAccountNumber());

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.AMOUNT_TRANSFERRED_CODE)
                .responseMessage(AccountUtils.AMOUNT_TRANSFERRED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(fromCustomer)) // Return sender's updated status
                .build();
    }

    private void executeBalanceUpdate(
            String accountNumber,
            BigDecimal amount,
            TransactionTypeOptions type,
            String remarks)
    {
        Customer customer = customerRepository.findByAccountNumber(accountNumber);

        if (type == TransactionTypeOptions.CREDIT)
        {
            customer.setAccountBalance(customer.getAccountBalance().add(amount));
        }
        else
        {
            customer.setAccountBalance(customer.getAccountBalance().subtract(amount));
        }

        customerRepository.save(customer);

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
    public GlobalResponse<CustomerResponse>
    createAccount(CustomerRequest customerRequest)
    {
        if (customerRepository.existsByEmail(customerRequest.getEmail()))
        {
            throw new EmailAlreadyExistsException();
        }

        if (customerRepository.existsByPhoneNumber(customerRequest.getEmail()))
        {
            throw new PhoneNumberAlreadyExistsException();
        }

        if (customerRequest.getAlternativePhoneNumber() != null
                && customerRepository.existsByAlternativePhoneNumber((customerRequest.getAlternativePhoneNumber())))
        {
            throw new AlternativePhoneNumberExistsException();
        }

        if (customerRepository.existsByAlternativePhoneNumber((customerRequest.getGovernmentId())))
        {
            throw new GovernmentIdExistsException();
        }

        // we are receiving a DTO (request) from the controller => convert to Entity => save in repo
        Customer newCustomer = customerMapper.requestToEntity(customerRequest);
        newCustomer.setAccountNumber(AccountUtils.generateAccountNumber());
        newCustomer.setAccountBalance(BigDecimal.ZERO);
        newCustomer.setDailyTransferLimit(AccountUtils.DEFAULT_TRANSFER_LIMIT);
        newCustomer.setBaseCurrency(CurrencyOptions.USD);
        newCustomer.setStatus(AccountStatusOptions.ACTIVE);

        Customer savedCustomer = customerRepository.save(newCustomer);

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
                savedCustomer.getFirstName(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                savedCustomer.getAccountNumber(),
                bankName);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(savedCustomer.getEmail())
                .subject("Account Created Successfully")
                .messageBody(htmlBody)
                .build();

        emailService.sendEmailNotification(emailDetailsDTO);// async

        CustomerResponse data = customerMapper.entityToResponse(savedCustomer);

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_CREATED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.CUSTOMER_CREATED_SUCCESSFULLY)
                .data(data)
                .build();
    }

    @Override
    public GlobalResponse<CustomerResponse>
    balanceEnquiry(EnquiryRequest enquiryRequest)
    {
        Customer foundCustomer = customerRepository.findByAccountNumber(enquiryRequest.getAccountNumber());
        if (foundCustomer == null)
        {
            throw new AccountNotFoundException();
        }

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_FOUND)
                .data(customerMapper.entityToResponse(foundCustomer))
                .build();
    }

    @Override
    @Transactional
    public GlobalResponse<CustomerResponse>
    debitAccount(CreditDebitRequest creditDebitRequest)
    {
        Customer customerToDebit = customerRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        // check if account exists
        if (customerToDebit == null)
        {
            throw new AccountNotFoundException();
        }

        BigDecimal amount = creditDebitRequest.getAmount();

        // validate against transfer limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        // check for sufficient funds
        if (customerToDebit.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        // update balance and save
        customerToDebit.setAccountBalance(customerToDebit.getAccountBalance().subtract(amount));
        customerRepository.save(customerToDebit);

        // log transaction async.
        transactionService.saveTransaction(TransactionRequest.builder()
                .amount(amount)
                .accountNumber(customerToDebit.getAccountNumber())
                .transactionType(TransactionTypeOptions.DEBIT)
                .status("COMPLETED")
                .remarks("Manual Debit")
                .build());

        // send notification (Async)
        sendTransactionEmail(customerToDebit, amount, "Debited");

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_DEBITED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.CUSTOMER_DEBITED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(customerToDebit))
                .build();
    }

    @Override
    @Transactional
    public GlobalResponse<CustomerResponse>
    creditAccount(CreditDebitRequest request)
    {
        Customer customerToCredit = customerRepository.findByAccountNumber(request.getAccountNumber());

        if (customerToCredit == null)
        {
            throw new AccountNotFoundException();
        }

        BigDecimal amount = request.getAmount();

        // validate against transfer limits
        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        customerToCredit.setAccountBalance(customerToCredit.getAccountBalance().add(request.getAmount()));
        customerRepository.save(customerToCredit);

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(customerToCredit))
                .build();
    }

    @Override
    public List<CustomerResponse> searchCustomers(CustomerSearchCriteria criteria)
    {
        Specification<Customer> spec = Specification
                .where(CustomerSpecs.isEquals(Customer_.EMAIL, criteria.getEmail()))
                .and(CustomerSpecs.isEquals(Customer_.ACCOUNT_NUMBER, criteria.getAccountNumber()))
                .and(CustomerSpecs.isAbove(criteria.getMinAge()));

        return customerRepository.findAll(spec)
                .stream()
                .map(customerMapper::entityToResponse)
                .collect(Collectors.toList());
    }
}
