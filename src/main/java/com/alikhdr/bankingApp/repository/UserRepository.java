package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID>
{
    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByAlternativePhoneNumber(String alternativePhoneNumber);

    Boolean existsByGovernmentId(String governmentId);

    Boolean existsByAccountNumber(String accountNumber);

    User findByAccountNumber(String accountNumber);
}
