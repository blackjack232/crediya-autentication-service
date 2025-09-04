package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequest;
import co.com.pragma.api.dto.response.ApiResponse;
import co.com.pragma.api.dto.response.UserResponse;
import co.com.pragma.api.mapper.UserRequestMapper;
import co.com.pragma.api.mapper.UserResponseMapper;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class Handler {

    private final UserUseCase userUseCase;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;

    public Mono<String> status() {
        return Mono.just("Auth service is running ✅");
    }
/*
    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(User.class)
                .flatMap(userUseCase::saveUser)
                .flatMap(savedUser -> {
                    ApiResponse<User> response = ApiResponse.<User>builder()
                            .message(UserMessages.USUARIO_CREADO)
                            .code(201)
                            .success(true)
                            .data(savedUser)
                            .build();

                    return ServerResponse.status(HttpStatus.CREATED) // 201
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(e -> {
                    ApiResponse<Object> errorResponse = ApiResponse.builder()
                            .message("Error: " + e.getMessage())
                            .code(400)
                            .success(false)
                            .data(null)
                            .build();

                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }*/

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserRequest.class)
                .map(userRequestMapper::toDomain)  // DTO → Domain
                .flatMap(userUseCase::saveUser)
                .map(userResponseMapper::toResponse) // Domain → DTO
                .flatMap(userResponse -> {
                    ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                            .message(UserMessages.USUARIO_CREADO)
                            .code(201)
                            .success(true)
                            .data(userResponse)
                            .build();

                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(e -> {
                    ApiResponse<Object> errorResponse = ApiResponse.builder()
                            .message("Error: " + e.getMessage())
                            .code(400)
                            .success(false)
                            .data(null)
                            .build();

                    return ServerResponse.badRequest()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(errorResponse);
                });
    }

}

