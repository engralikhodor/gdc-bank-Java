package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.AuthLoginRequest;
import com.alikhdr.bankingApp.dto.AuthRegisterRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.GlobalResponse;
import com.alikhdr.bankingApp.entity.AccountStatusOptions;
import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.exception.UsernameAlreadyUsedException;
import com.alikhdr.bankingApp.mapper.AuthMapper;
import com.alikhdr.bankingApp.mapper.CustomerMapper;
import com.alikhdr.bankingApp.repository.AuthRepository;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.AuthService;
import com.alikhdr.bankingApp.service.JwtService;
import com.alikhdr.bankingApp.utils.AccountUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthImpl implements AuthService
{
    private final AuthRepository authRepository;
    private final AuthMapper authMapper;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public GlobalResponse<AuthResponse> register(AuthRegisterRequest request)
    {
        if (authRepository.existsByUsername(request.getUsername()))
        {
            throw new UsernameAlreadyUsedException();
        }

        Customer customer = customerMapper.requestToEntity(request.getCustomerRequest());

        //  set defaults
        customer.setAccountNumber(AccountUtils.generateAccountNumber());
        customer.setAccountBalance(BigDecimal.ZERO);
        customer.setStatus(AccountStatusOptions.ACTIVE);

        // map auth
        Auth auth = authMapper.requestToEntity(request);
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        auth.setRole(request.getRole());

        // link both sides (Bi-directional)
        customer.setAuth(auth);
        auth.setCustomer(customer);

        // save (Cascade saves Auth)
        customerRepository.save(customer);

        return GlobalResponse.<AuthResponse>builder()
                .responseCode("201")
                .responseMessage("User registered successfully")
                .data(AuthResponse.builder()
                        .username(auth.getUsername())
                        .accountNumber(customer.getAccountNumber())
                        .firstName(customer.getFirstName())
                        .build())
                .build();
    }

    @Override
    public GlobalResponse<AuthResponse> login(AuthLoginRequest authLoginRequest)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authLoginRequest.getUsername(),
                        authLoginRequest.getPassword()
                )
        );

        Auth user = authRepository.findByUsername(authLoginRequest.getUsername()).orElseThrow();

        return GlobalResponse.<AuthResponse>builder()
                .responseCode("200")
                .responseMessage("Login Successful")
                .data(AuthResponse.builder()
                        .username(user.getUsername())
                        .token(jwtService.generateToken(user))
                        .build())
                .build();
    }
}
