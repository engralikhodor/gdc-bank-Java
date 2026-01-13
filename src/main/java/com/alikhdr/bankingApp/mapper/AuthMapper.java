package com.alikhdr.bankingApp.mapper;

import com.alikhdr.bankingApp.dto.AuthRequest;
import com.alikhdr.bankingApp.dto.AuthResponse;
import com.alikhdr.bankingApp.entity.Auth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper
{
    AuthResponse entityToResponse(Auth auth);

    Auth requestToEntity(AuthRequest authRequest);
}
