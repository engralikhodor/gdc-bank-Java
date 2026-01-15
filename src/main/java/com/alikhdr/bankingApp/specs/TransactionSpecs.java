package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.TransactionTypeOptions;
import com.alikhdr.bankingApp.entity.Transaction_;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecs
{

    public static Specification<Transaction> isEquals(String attribute, String value)
    {
        return (root, query, builder) ->
        {
            if (value == null || value.isEmpty())
            {
                return builder.conjunction();
            }
            return builder.equal(root.get(attribute), value);
        };
    }

    
    public static Specification<Transaction> isType(TransactionTypeOptions type)
    {
        return (root, query, builder) ->
        {
            if (type == null)
            {
                return builder.conjunction();
            }
            return builder.equal(root.get(Transaction_.TRANSACTION_TYPE), type);
        };
    }
}
