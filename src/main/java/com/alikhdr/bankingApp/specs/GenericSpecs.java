package com.alikhdr.bankingApp.specs;

import org.springframework.data.jpa.domain.Specification;

public class GenericSpecs
{
    // works for all types Customer, Transaction, etc.
    public static <T> Specification<T> isEquals(String field, Object value)
    {
        // root <=> table - query <=> overall SQL query - cb <=> tool used to create the logic
        return (root, query, cb) ->
                value == null ? cb.conjunction() : cb.equal(root.get(field), value);
    }
}
