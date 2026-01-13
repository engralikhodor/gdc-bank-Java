package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.entity.Customer_;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CustomerSpecs
{
    public static <T> Specification<Customer>
    isEquals(String fieldName, T value)
    {
        return ((root, query, criteriaBuilder) ->
                value == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get(fieldName), value));
    }

    public static Specification<Customer> isAbove(Integer minAge)
    {
        return (root, query, cb) ->
        {
            if (minAge == null)
            {
                return cb.conjunction(); // no filtering
            }

            LocalDate cutoffDate = LocalDate.now().minusYears(minAge);
            return cb.lessThanOrEqualTo(root.get(Customer_.DATE_OF_BIRTH), cutoffDate);
        };
    }

}
