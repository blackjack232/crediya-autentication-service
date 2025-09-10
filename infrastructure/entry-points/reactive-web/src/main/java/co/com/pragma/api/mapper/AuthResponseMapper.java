package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.AuthResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthResponseMapper {
    default AuthResponse toDto(String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .build();
    }
}