package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.Transaction;
import com.alikhdr.bankingApp.entity.Transaction_;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TransactionSpecs
{
    // EQUAL
    public static <T> Specification<Transaction>
    isEquals(String fieldName, T value)
    {
        return (root, query, cb) ->
                value == null ? null : cb.equal(root.get(fieldName), value);
    }

    // LIKE
    public static Specification<Transaction>
    containsText(String fieldName, String text)
    {
        return (root, query, cb) ->
        {
            if (text == null || text.isBlank())
                return null;
            return cb.like(cb.lower(root.get(fieldName)), "%" + text.toLowerCase() + "%");
        };
    }

    // BETWEEN
    public static Specification<Transaction>
    amountBetween(BigDecimal min, BigDecimal max)
    {
        return (root, query, cb) ->
        {
            if (min != null && max != null)
                return cb.between(root.get(Transaction_.AMOUNT), min, max);
            if (min != null)
                return cb.greaterThanOrEqualTo(root.get(Transaction_.AMOUNT), min);
            if (max != null)
                return cb.lessThanOrEqualTo(root.get(Transaction_.AMOUNT), max);
            return null;
        };
    }
}
