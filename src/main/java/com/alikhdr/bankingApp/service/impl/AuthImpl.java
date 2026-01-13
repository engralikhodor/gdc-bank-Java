package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.AuthRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.entity.RoleOptions;
import com.alikhdr.bankingApp.exception.UsernameAlreadyUsedException;
import com.alikhdr.bankingApp.mapper.AuthMapper;
import com.alikhdr.bankingApp.repository.AuthRepository;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.AuthService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthImpl implements AuthService
{
    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public GlobalResponse<AuthResponse> register(AuthRequest authRequest)
    {
        // check if username is taken
        if (authRepository.existsByUsername(authRequest.getUsername()))
        {
            throw new UsernameAlreadyUsedException();
        }

        // retrieve User
        Customer customer = customerRepository.findById(authRequest.getCustomer_id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // check if this user already has credentials
        if (authRepository.existsByCustomer(customer))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has registered credentials");
        }

        Auth newAuth = authMapper.requestToEntity(authRequest);
        newAuth.setCustomer(customer);
        newAuth.setRole(RoleOptions.CUSTOMER);

        newAuth.setPassword(passwordEncoder.encode(authRequest.getPassword()));

        authRepository.save(newAuth);

        AuthResponse data = AuthResponse.builder()
                .firstName(customer.getFirstName())
                .accountNumber(customer.getAccountNumber())
                .build();

        return GlobalResponse.<AuthResponse>builder()
                .responseCode(AccountUtils.USERNAME_CREATED_SUCCESSFULLY_CODE)
                .responseMessage(AccountUtils.USERNAME_CREATED_SUCCESSFULLY)
                .data(data)
                .build();
    }
}
