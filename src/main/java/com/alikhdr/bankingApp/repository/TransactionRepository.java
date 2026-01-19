package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends
        JpaRepository<Transaction, UUID>,
        JpaSpecificationExecutor<Transaction>
{
    List<Transaction> findByDestinationAccountNumber(String accountNumber);
}
