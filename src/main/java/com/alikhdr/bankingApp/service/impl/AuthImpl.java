package com.alikhdr.bankingApp.service.impl;

import com.alikhdr.bankingApp.dto.AuthLoginRequest;
import com.alikhdr.bankingApp.dto.AuthRegisterRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.dto.TokenRefreshRequest;
import com.alikhdr.bankingApp.entity.*;
import com.alikhdr.bankingApp.exception.AccountNotFoundException;
import com.alikhdr.bankingApp.exception.InvalidRefreshTokenException;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthImpl implements AuthService
{
    private static final Logger log = LoggerFactory.getLogger(AuthImpl.class);

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
    public AuthResponse register(AuthRegisterRequest request)
    {
        if (authRepository.existsByUsername(request.getUsername()))
        {
            log.warn("Registration attempt with existing username: {}", request.getUsername());
            throw new UsernameAlreadyUsedException();
        }

        Customer customer = customerMapper.requestToEntity(request.getCustomerRequest());

        //  set defaults
        customer.setAccountNumber(AccountUtils.generateAccountNumber());
        customer.setAccountBalance(BigDecimal.ZERO);
        customer.setStatus(AccountStatusOptions.ACTIVE);

        customer.setDailyTransferLimit(new BigDecimal("500.00"));
        customer.setBaseCurrency(CurrencyOptions.USD);

        // map auth
        Auth auth = authMapper.requestToEntity(request);
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        // Enforce default role for new registrations
        auth.setRole(RoleOptions.CUSTOMER);

        // link both sides (Bi-directional)
        customer.setAuth(auth);
        auth.setCustomer(customer);

        // save (Cascade saves Auth)
        customerRepository.save(customer);

        log.info("User registered successfully: username={}, firstName={}, accountNumber={}",
                auth.getUsername(), customer.getFirstName(), customer.getAccountNumber());

        return AuthResponse.builder()
                .username(auth.getUsername())
                .accountNumber(customer.getAccountNumber())
                .firstName(customer.getFirstName())
                .build();
    }

    @Override
    public AuthResponse login(AuthLoginRequest authLoginRequest)
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

        log.info("User logged in successfully: username={}", user.getUsername());

        return AuthResponse.builder()
                .username(user.getUsername())
                .firstName(customer.getFirstName())
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())//24 hrs
                .build();
    }

    @Override
    public AuthResponse refreshToken(TokenRefreshRequest request)
    {
        RefreshToken tokenInDb = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        // check if it is still valid (not older than 24 hours)
        refreshTokenService.validateTokenExpiration(tokenInDb);

        Auth auth = tokenInDb.getAuth();

        // get relevant Customer from Auth
        Customer customer = customerRepository.findByEmail(auth.getUsername())
                .orElseThrow(AccountNotFoundException::new);

        // new 15-minute Access Token
        String newAccessToken = jwtService.generateToken(auth);

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
        log.info("Token refreshed successfully for username: {}", auth.getUsername());
        return authResponse;
    }
}
