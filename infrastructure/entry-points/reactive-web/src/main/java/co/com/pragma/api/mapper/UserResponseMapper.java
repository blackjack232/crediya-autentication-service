package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.response.UserResponse;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserResponseMapper {
    UserResponse toResponse(User user);
}