package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.CustomerRequest;
import com.alikhdr.bankingApp.dto.CustomerResponse;
import com.alikhdr.bankingApp.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper
{
    // Entity -> Response (Output)
    CustomerResponse entityToResponse(Customer customer);

    // Request -> Entity (Input)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "accountBalance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    Customer requestToEntity(CustomerRequest customerRequest);
}
