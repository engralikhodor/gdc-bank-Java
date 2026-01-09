package com.alikhdr.bankingApp;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.User;
import com.alikhdr.bankingApp.mapper.UserMapper;
import com.alikhdr.bankingApp.repository.UserRepository;
import com.alikhdr.bankingApp.service.EmailService;
import com.alikhdr.bankingApp.service.TransactionService;
import com.alikhdr.bankingApp.service.impl.UserImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserImpl userService;

    @Test
    @DisplayName("Should return a list of user when search criteria matches")
    void searchUsers_Success()
    {
        User mockUser = new User();
        mockUser.setFirstName("John");

        UserResponse mockResponse = UserResponse.builder().accountName("John Doe").build();

        // mocking JPA specs. and mapper
        when(userRepository.findAll(any(Specification.class))).thenReturn(List.of(mockUser));
        when(userMapper.entityToResponse(any(User.class))).thenReturn(mockResponse);

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setEmail("test@test.com");
        List<UserResponse> results = userService.searchUsers(criteria);

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).accountName());
        verify(userRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Should fail debit when balance is insufficient")
    void debitAccount_InsufficientBalance()
    {
        // Arrange
        CreditDebitRequest request = new CreditDebitRequest("12345", new BigDecimal("5000.00"));
        User mockUser = new User();
        mockUser.setAccountNumber("12345");
        mockUser.setAccountBalance(new BigDecimal("1000.00"));

        when(userRepository.existsByAccountNumber("12345")).thenReturn(true);
        when(userRepository.findByAccountNumber("12345")).thenReturn(mockUser);

        ApiResponse<UserResponse> response = userService.debitAccount(request);

        assertEquals(AccountUtils.INSUFFICIENT_BALANCE_CODE, response.responseCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully credit user account and send notification")
    void creditAccount_Success()
    {
        CreditDebitRequest request = new CreditDebitRequest("12345", new BigDecimal("5000.00"));
        User mockUser = new User();
        mockUser.setFirstName("John");
        mockUser.setAccountNumber("12345");
        mockUser.setAccountBalance(new BigDecimal("1000.00"));
        mockUser.setEmail("john@example.com");

        when(userRepository.existsByAccountNumber("12345")).thenReturn(true);
        when(userRepository.findByAccountNumber("12345")).thenReturn(mockUser);

        ApiResponse<UserResponse> response = userService.creditAccount(request);

        // check results
        assertEquals(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE, response.responseCode());
        assertEquals(new BigDecimal("1500.00"), response.data().accountBalance());

        // check behavior
        verify(userRepository, times(1)).save(any(User.class));
        verify(transactionService, times(1)).saveTransaction(any(TransactionRequest.class));
        verify(emailService, times(1)).sendEmailNotification(any(EmailDetailsDTO.class));
    }
}
