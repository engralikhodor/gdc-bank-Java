package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.TransactionRequest;
import com.alikhdr.bankingApp.dto.TransactionResponse;
import com.alikhdr.bankingApp.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper
{

    // Entity -> Response (Read)
    TransactionResponse entityToResponse(Transaction transaction);

    // Request -> Entity (Write)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Transaction requestToEntity(TransactionRequest dto);
}
