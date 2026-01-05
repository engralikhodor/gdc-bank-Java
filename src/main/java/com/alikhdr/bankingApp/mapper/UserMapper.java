package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.UserRequestDTO;
import com.alikhdr.bankingApp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper
{
    /**
     * Maps UserRequest to User Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "accountBalance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dailyTransferLimit", ignore = true)
    @Mapping(target = "baseCurrency", ignore = true)
    @Mapping(target = "isEmailVerified", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    //    @Mapping(target = "gender", ignore = true)
    User mapRequestToEntity(UserRequestDTO userRequestDTO);
}
