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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
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
    public CustomerResponse openInitialAccount(CustomerRequest customerRequest)
    {
        if (customerRepository.existsByEmail(customerRequest.getEmail()))
        {
            throw new EmailAlreadyExistsException();
        }
        if (customerRepository.existsByPhoneNumber(customerRequest.getPhoneNumber()))
        {
            throw new PhoneNumberAlreadyExistsException();
        }
        if (customerRepository.existsByAlternativePhoneNumber(customerRequest.getAlternativePhoneNumber()))
        {
            throw new AlternativePhoneNumberExistsException();
        }
        if (customerRepository.existsByGovernmentId(customerRequest.getGovernmentId()))
        {
            throw new GovernmentIdExistsException();
        }

        Customer newCustomer = Customer.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .otherName(customerRequest.getOtherName())
                .gender(customerRequest.getGender())
                .address(customerRequest.getAddress())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .alternativePhoneNumber(customerRequest.getAlternativePhoneNumber())
                .status(AccountStatusOptions.ACTIVE)
                .governmentId(customerRequest.getGovernmentId())
                .baseCurrency(CurrencyOptions.USD)
                .build();

        Customer savedCustomer = customerRepository.save(newCustomer);

        EmailDetailsDTO emailDetailsDTO = EmailDetailsDTO.builder()
                .recipient(savedCustomer.getEmail())
                .subject("Account Created")
                .messageBody("Congratulations! Your account has been successfully created.\n Your Account Details: \n" +
                        "Account Name: " + savedCustomer.getFirstName() + " " + savedCustomer.getLastName() + "\n" +
                        "Account Number: " + savedCustomer.getAccountNumber())
                .build();

        emailService.sendEmailAlert(emailDetailsDTO);

        return customerMapper.entityToResponse(savedCustomer);
    }

    @Override
    public GlobalResponse<CustomerResponse> balanceEnquiry(EnquiryRequest enquiryRequest)
    {
        boolean isAccountExist = customerRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());

        if (!isAccountExist)
        {
            throw new AccountNotFoundException();
        }

        Customer foundCustomer = customerRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_FOUND)
                .data(customerMapper.entityToResponse(foundCustomer))
                .build();
    }

    @Override
    public GlobalResponse<String> nameEnquiry(EnquiryRequest enquiryRequest)
    {
        boolean isAccountExist = customerRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());

        if (!isAccountExist)
        {
            throw new AccountNotFoundException();
        }

        Customer foundCustomer = customerRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return GlobalResponse.<String>builder()
                .responseCode(AccountUtils.CUSTOMER_FOUND_CODE)
                .responseMessage(AccountUtils.CUSTOMER_FOUND)
                .data(foundCustomer.getFirstName() + " " + foundCustomer.getLastName() + " " + foundCustomer.getOtherName())
                .build();
    }

    @Override
    @Transactional
    public GlobalResponse<CustomerResponse> creditAccount(CreditDebitRequest request)
    {
        Customer customerToCredit = customerRepository.findByAccountNumber(request.getAccountNumber());

        if (customerToCredit == null)
        {
            throw new AccountNotFoundException();
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(AccountUtils.DEFAULT_TRANSFER_LIMIT) > 0)
        {
            throw new ExceedsTransferLimitException();
        }

        customerToCredit.setAccountBalance(customerToCredit.getAccountBalance().add(request.getAmount()));
        customerRepository.save(customerToCredit);

        transactionService.saveTransaction(TransactionRequest.builder()
                .accountNumber(customerToCredit.getAccountNumber())
                .transactionType(TransactionTypeOptions.CREDIT)
                .amount(request.getAmount())
                .status(TransactionStatusOptions.COMPLETED.name())
                .remarks("Account Credited via API")
                .build());

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(customerToCredit))
                .build();
    }

    @Override
    @Transactional
    public GlobalResponse<CustomerResponse> debitAccount(CreditDebitRequest request)
    {
        Customer customerToDebit = customerRepository.findByAccountNumber(request.getAccountNumber());

        if (customerToDebit == null)
        {
            throw new AccountNotFoundException();
        }

        BigDecimal amount = request.getAmount();

        if (customerToDebit.getAccountBalance().compareTo(amount) < 0)
        {
            throw new InsufficientResourcesException();
        }

        customerToDebit.setAccountBalance(customerToDebit.getAccountBalance().subtract(request.getAmount()));
        customerRepository.save(customerToDebit);

        transactionService.saveTransaction(TransactionRequest.builder()
                .accountNumber(customerToDebit.getAccountNumber())
                .transactionType(TransactionTypeOptions.DEBIT)
                .amount(request.getAmount())
                .status(TransactionStatusOptions.COMPLETED.name())
                .remarks("Account Debited via API")
                .build());

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.CUSTOMER_DEBITED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.CUSTOMER_DEBITED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(customerToDebit))
                .build();
    }

    @Override
    public GlobalResponse<CustomerResponse> transferAmount(TransferRequest transferRequest)
    {
        TransactionResponse txResponse = transactionService.transferAmount(transferRequest);

        Customer fromCustomer = customerRepository.findByAccountNumber(transferRequest.getFromAccountNumber());

        return GlobalResponse.<CustomerResponse>builder()
                .responseCode(AccountUtils.AMOUNT_TRANSFERRED_CODE)
                .responseMessage(AccountUtils.AMOUNT_TRANSFERRED_SUCCESSFULLY)
                .data(customerMapper.entityToResponse(fromCustomer))
                .build();
    }

    @Override
    public GlobalResponse<List<CustomerResponse>> searchCustomers(CustomerSearchCriteria criteria)
    {
        Specification<Customer> spec = Specification
                .where(CustomerSpecs.hasEmail(criteria.getEmail()))
                .and(CustomerSpecs.hasAccountNumber(criteria.getAccountNumber()))
                .and(CustomerSpecs.isAbove(criteria.getMinAge()));

        List<CustomerResponse> results = customerRepository.findAll(spec)
                .stream()
                .map(customerMapper::entityToResponse)
                .collect(Collectors.toList());

        return GlobalResponse.<List<CustomerResponse>>builder()
                .responseCode(String.valueOf(HttpStatus.OK.value()))
                .responseMessage(results.isEmpty() ? "No customers found matching criteria" : "Customers retrieved successfully")
                .data(results)
                .build();
    }
}
