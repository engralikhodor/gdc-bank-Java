package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long>
{
    Optional<RefreshToken> findByToken(String token);

    void deleteByAuth(Auth auth);
}
