package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.model.auth.Auth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginRequestMapper {
    Auth toDomain(LoginRequest request);
}
