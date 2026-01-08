package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.TransactionDTO;
import com.alikhdr.bankingApp.dto.TransactionResponseDTO;
import com.alikhdr.bankingApp.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper
{
    // Entity => DTO (used by search results)
    TransactionResponseDTO entityToDto(Transaction transaction);

    // DTO => Entity
    Transaction dtoToEntity(TransactionDTO dto);
}
