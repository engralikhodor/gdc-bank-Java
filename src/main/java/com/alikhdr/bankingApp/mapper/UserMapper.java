package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.UserRequest;
import com.alikhdr.bankingApp.dto.UserResponse;
import com.alikhdr.bankingApp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper
{
    // Entity -> Response (Output)
    UserResponse entityToResponse(User user);

    // Request -> Entity (Input)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "accountBalance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    User requestToEntity(UserRequest userRequest);
}
