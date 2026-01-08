package com.alikhdr.bankingApp.specs;

import com.alikhdr.bankingApp.entity.User;
import com.alikhdr.bankingApp.entity.User_;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class UserSpecs
{
    public static <T> Specification<User>
    isEquals(String fieldName, T value)
    {
        return ((root, query, criteriaBuilder) ->
                value == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get(fieldName), value));
    }

    public static Specification<User> isAbove(Integer minAge)
    {
        return (root, query, cb) ->
        {
            if (minAge == null)
            {
                return cb.conjunction(); // no filtering
            }

            LocalDate cutoffDate = LocalDate.now().minusYears(minAge);
            return cb.lessThanOrEqualTo(root.get(User_.DATE_OF_BIRTH), cutoffDate);
        };
    }

}
