package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import com.alikhdr.bankingApp.entity.Transaction_;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecs
{
    public static Specification<Transaction> hasAccountNumber(String accNum)
    {
        return GenericSpecs.isEquals(Transaction_.DESTINATION_ACCOUNT_NUMBER, accNum);
    }

    public static Specification<Transaction> isType(TransactionTypeOptions type)
    {
        return GenericSpecs.isEquals(Transaction_.TRANSACTION_TYPE, type);
    }
}
