package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.entity.Customer_;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CustomerSpecs
{
    public static Specification<Customer> hasEmail(String email)
    {
        return GenericSpecs.isEquals(Customer_.EMAIL, email);
    }

    public static Specification<Customer> hasAccountNumber(String accountNumber)
    {
        return GenericSpecs.isEquals(Customer_.ACCOUNT_NUMBER, accountNumber);
    }

    public static Specification<Customer> isAbove(Integer minAge)
    {
        return (root, query, cb) -> minAge == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get(Customer_.DATE_OF_BIRTH), LocalDate.now().minusYears(minAge));
    }
}
