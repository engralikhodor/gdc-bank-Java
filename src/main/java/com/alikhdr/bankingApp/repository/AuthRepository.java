package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Auth;
import com.alikhdr.bankingApp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository
        extends JpaRepository<Auth, UUID>
{
    Optional<Auth> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByCustomer(Customer customer);
}
