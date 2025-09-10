package co.com.pragma.api;


import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.api.dto.response.ApiResponse;
import co.com.pragma.api.dto.response.AuthResponse;
import co.com.pragma.api.mapper.AuthResponseMapper;
import co.com.pragma.api.mapper.LoginRequestMapper;
import co.com.pragma.usecase.auth.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
@RestController
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthUseCase authUseCase;
    private final LoginRequestMapper loginRequestMapper;
    private final AuthResponseMapper authResponseMapper;

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginRequest.class)
                .map(loginRequestMapper::toDomain)
                .flatMap(authUseCase::login)
                .map(authResponseMapper::toDto)
                .flatMap(authResponse -> {
                    ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                            .message("Login exitoso")
                            .code(200)
                            .success(true)
                            .data(authResponse)
                            .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(e -> {
                    ApiResponse<Object> error = ApiResponse.builder()
                            .message("Error: " + e.getMessage())
                            .code(401)
                            .success(false)
                            .build();
                    return ServerResponse.status(401)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(error);
                });
    }


}