package co.com.pragma.api.mapper;
import co.com.pragma.api.dto.request.UserRequest;
import co.com.pragma.model.user.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface UserRequestMapper {
    User toDomain(UserRequest request);
}