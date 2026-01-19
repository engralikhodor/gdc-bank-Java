package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;


public interface CustomerRepository
        extends JpaRepository<Customer, UUID>,
        JpaSpecificationExecutor<Customer>
{
    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    Boolean existsByAlternativePhoneNumber(String alternativePhoneNumber);

    Boolean existsByGovernmentId(String governmentId);

    Boolean existsByAccountNumber(String accountNumber);

    Optional<Customer> findByAccountNumber(String accountNumber);
}
