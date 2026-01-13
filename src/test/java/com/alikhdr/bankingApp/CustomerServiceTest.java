package com.alikhdr.bankingApp;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.CurrencyOptions;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.mapper.CustomerMapper;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.EmailService;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.service.impl.CustomerImpl;
import com.alikhdr.bankingApp.utils.AccountUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest
{

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EmailService emailService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerImpl customerservice;

    @Test
    @DisplayName("Should return a list of customer when search criteria matches")
    void searchCustomer_Success()
    {
        Customer mockCustomer = new Customer();
        mockCustomer.setFirstName("John");
        CustomerResponse mockResponse = CustomerResponse.builder().accountName("John Doe").build();

        when(customerRepository.findAll(any(Specification.class))).thenReturn(List.of(mockCustomer));
        when(customerMapper.entityToResponse(any(Customer.class))).thenReturn(mockResponse);

        CustomerSearchCriteria criteria = new CustomerSearchCriteria();
        criteria.setEmail("test@test.com");
        List<CustomerResponse> results = customerservice.searchCustomers(criteria);

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).accountName());
        verify(customerRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should fail debit when balance is insufficient")
    void debitAccount_InsufficientBalance()
    {
        CreditDebitRequest request = new CreditDebitRequest("12345", new BigDecimal("5000.00"));
        Customer mockCustomer = new Customer();
        mockCustomer.setAccountNumber("12345");
        mockCustomer.setAccountBalance(new BigDecimal("1000.00"));

        when(customerRepository.findByAccountNumber("12345")).thenReturn(mockCustomer);

        GlobalResponse<CustomerResponse> response = customerservice.debitAccount(request);

        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, response.responseCode());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should successfully credit customer account and send notification")
    void creditAccount_Success()
    {
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal creditAmount = new BigDecimal("500.00");
        BigDecimal finalBalance = new BigDecimal("1500.00");

        CreditDebitRequest request = new CreditDebitRequest("12345", creditAmount);
        Customer mockCustomer = new Customer();
        mockCustomer.setFirstName("John");
        mockCustomer.setAccountNumber("12345");
        mockCustomer.setAccountBalance(initialBalance);
        mockCustomer.setEmail("john@example.com");

        CustomerResponse mockResponse = CustomerResponse.builder()
                .accountBalance(finalBalance)
                .accountName("John Doe")
                .build();

        when(customerRepository.findByAccountNumber("12345")).thenReturn(mockCustomer);
        when(customerMapper.entityToResponse(any(Customer.class))).thenReturn(mockResponse);

        GlobalResponse<CustomerResponse> response = customerservice.creditAccount(request);

        assertNotNull(response.data());
        assertEquals(AccountUtils.CUSTOMER_CREDITED_SUCCESSFULLY_CODE, response.responseCode());
        assertEquals(finalBalance, response.data().accountBalance());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should return account not found response for invalid account number")
    void nameEnquiry_AccountNotFound()
    {
        // Arrange
        EnquiryRequest request = new EnquiryRequest();
        request.setAccountNumber("999");

        // Mocking the check that fails first
        when(customerRepository.existsByAccountNumber("999")).thenReturn(false);

        // Act
        GlobalResponse<String> response = customerservice.nameEnquiry(request);

        // Assert
        assertNotNull(response);
        assertEquals(AccountUtils.CUSTOMER_NOT_FOUND_CODE, response.responseCode());
        assertEquals(AccountUtils.CUSTOMER_NOT_FOUND, response.responseMessage());
        assertNull(response.data()); // Data should be null if not found

        // Verify interactions
        verify(customerRepository, times(1)).existsByAccountNumber("999");
    }

    @Test
    @DisplayName("Should successfully transfer money between two valid accounts")
    void transferAmount_Success()
    {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("111")
                .toAccountNumber("222")
                .amountToTransfer(new BigDecimal("200.00"))
                .remarks("Test transfer")
                .build();

        Customer fromCustomer = new Customer();
        fromCustomer.setAccountNumber("111");
        fromCustomer.setAccountBalance(new BigDecimal("1000.00"));
        fromCustomer.setBaseCurrency(CurrencyOptions.USD);

        Customer toCustomer = new Customer();
        toCustomer.setAccountNumber("222");
        toCustomer.setAccountBalance(new BigDecimal("500.00"));
        toCustomer.setBaseCurrency(CurrencyOptions.USD);

        when(customerRepository.findByAccountNumber("111")).thenReturn(fromCustomer);
        when(customerRepository.findByAccountNumber("222")).thenReturn(toCustomer);

        GlobalResponse response = customerservice.transferAmount(request);

        assertEquals(AccountUtils.AMOUNT_TRANSFERRED_CODE, response.responseCode());
        verify(customerRepository, times(2)).save(any(Customer.class));
        verify(transactionService, times(2)).saveTransaction(any(TransactionRequest.class));

    }

    @Test
    @DisplayName("Should fail transfer when source and destination accounts are the same")
    void transferAmount_SameAccount_Failure()
    {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("111")
                .toAccountNumber("111") // Same account
                .amountToTransfer(new BigDecimal("200.00"))
                .build();

        Customer mockCustomer = new Customer();
        mockCustomer.setAccountNumber("111");

        // We mock the repository to return the same customer for both lookups
        when(customerRepository.findByAccountNumber("111")).thenReturn(mockCustomer);

        // Act
        GlobalResponse response = customerservice.transferAmount(request);

        // Assert
        assertEquals(AccountUtils.SAME_ACCOUNT_TRANSFER_CODE, response.responseCode());
        assertEquals(AccountUtils.SAME_ACCOUNT_TRANSFER, response.responseMessage());

        // Verify no money was actually moved (save was never called)
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
