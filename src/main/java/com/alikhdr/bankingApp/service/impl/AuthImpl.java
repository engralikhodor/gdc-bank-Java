package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.*;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.exception.AccountNotFoundException;
import com.alikhdr.bankingApp.exception.UsernameAlreadyUsedException;
import com.alikhdr.bankingApp.mapper.AuthMapper;
import com.alikhdr.bankingApp.mapper.CustomerMapper;
import com.alikhdr.bankingApp.repository.AuthRepository;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.repository.RefreshTokenRepository;
import com.alikhdr.bankingApp.service.AuthService;
import com.alikhdr.bankingApp.service.JwtService;
import com.alikhdr.bankingApp.service.RefreshTokenService;
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
    private final RefreshTokenService refreshTokenService;
    private final CustomerMapper customerMapper;
    private final RefreshTokenRepository refreshTokenRepository;

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

        // These two were missing and caused the 500 error:
        customer.setDailyTransferLimit(new BigDecimal("500.00")); // Set a default limit
        customer.setBaseCurrency(CurrencyOptions.USD); // Set a default currency
        
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

        Auth user = authRepository.findByUsername(authLoginRequest.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        // generate both tokens (access+refresh)
        String jwtToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        Customer customer = customerRepository.findByEmail(user.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        return GlobalResponse.<AuthResponse>builder()
                .responseCode("200")
                .responseMessage("Login Successful")
                .data(AuthResponse.builder()
                        .username(user.getUsername())
                        .firstName(customer.getFirstName())
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken.getToken())//24 hrs
                        .build())
                .build();
    }

    public GlobalResponse<AuthResponse> refreshToken(TokenRefreshRequest request)
    {
        // get the token from the database
        RefreshToken tokenInDb = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));

        // check if it is still valid (not older than 24 hours)
        refreshTokenService.validateTokenExpiration(tokenInDb);

        // get Auth
        Auth auth = tokenInDb.getAuth();
        // get relevant Customer from Auth
        Customer customer = customerRepository.findByEmail(auth.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        // generate a brand new 15-minute Access Token
        String newAccessToken = jwtService.generateToken(auth);

        // build the Response
        AuthResponse authResponse = AuthResponse.builder()
                .username(auth.getUsername())
                .firstName(customer.getFirstName())
                .accessToken(newAccessToken)
                .refreshToken(request.refreshToken())
                .build();
       /*
        ** OR we can use this shortcut
        AuthResponse authResponse = refreshTokenRepository.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(token ->
                {
                    Auth auth = token.getAuth();
                    String newAccessToken = jwtService.generateToken(auth);
                    Customer customer = customerRepository.findByEmail(auth.getUsername())
                            .orElseThrow(AccountNotFoundException::new);

                    return AuthResponse.builder()
                            .username(auth.getUsername())
                            .firstName(customer.getFirstName())
                            .accessToken(newAccessToken)
                            .refreshToken(request.refreshToken()) // Keep using the same 24h key
                            .build();
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
*/
        return GlobalResponse.<AuthResponse>builder()
                .responseCode("200")
                .responseMessage("Token refreshed successfully")
                .data(authResponse)
                .build();
    }
}
