package com.alikhdr.bankingApp;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.Customer;
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

    @InjectMocks
    private CustomerImpl customerService;

    @Test
    @DisplayName("Should transfer amount successfully and return a wrapped GlobalResponse")
    void transferAmount_Success()
    {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("111")
                .destinationAccountNumber("222")
                .amountToTransfer(new BigDecimal("100.00"))
                .build();

        Customer sender = new Customer();
        sender.setAccountNumber("111");
        sender.setAccountBalance(new BigDecimal("500.00"));

        Customer receiver = new Customer();
        receiver.setAccountNumber("222");

        when(customerRepository.findByAccountNumber("111")).thenReturn(sender);
        when(customerRepository.findByAccountNumber("222")).thenReturn(receiver);

        TransactionResponse mockTxResponse = TransactionResponse.builder()
                .accountNumber("111")
                .amount(new BigDecimal("100.00"))
                .build();

        when(transactionService.transfer(any(TransferRequest.class))).thenReturn(mockTxResponse);

        // Act
        GlobalResponse<CustomerResponse> response = customerService.transferAmount(request);

        // Assert
        assertEquals(AccountUtils.TRANSFER_SUCCESSFUL_CODE, response.responseCode());
        verify(transactionService, times(1)).transfer(any(TransferRequest.class));
    }

    @Test
    @DisplayName("Should fail transfer when accounts are identical")
    void transferAmount_SameAccount_Failure()
    {
        TransferRequest request = TransferRequest.builder()
                .fromAccountNumber("111")
                .destinationAccountNumber("111")
                .amountToTransfer(new BigDecimal("200.00"))
                .build();

        // Business logic throws exception before reaching repo/tx service
        assertThrows(RuntimeException.class, () -> customerService.transferAmount(request));

        verify(transactionService, never()).transfer(any());
    }

    @Test
    @DisplayName("Should return GlobalResponse wrapper for search results")
    void searchCustomers_Success()
    {
        CustomerSearchCriteria criteria = new CustomerSearchCriteria();
        criteria.setEmail("email@test.com");
        criteria.setAccountNumber("111");
        criteria.setMinAge(18);

        // Act
        GlobalResponse<List<CustomerResponse>> response = customerService.searchCustomers(criteria);

        // Assert
        assertNotNull(response);
        assertEquals("200", response.responseCode());
        assertTrue(response.data() instanceof List);
    }
}
