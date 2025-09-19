/* package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequest;
import co.com.pragma.api.dto.response.ApiResponse;
import co.com.pragma.api.dto.response.UserResponse;
import co.com.pragma.api.mapper.UserRequestMapper;
import co.com.pragma.api.mapper.UserResponseMapper;
import co.com.pragma.model.user.constants.UserConstants;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class UserHandler {

    private final UserUseCase userUseCase;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;


    public Mono<String> status() {
        return Mono.just("Auth service is running ✅");
    }
    // Nuevo método: verificar existencia de usuario
    public Mono<ServerResponse> existsUserByIdentification(ServerRequest serverRequest) {
        String identification = serverRequest.pathVariable(UserConstants.IDENTIFICATION);
        return userUseCase.existsUserByIdentification(identification)
                .flatMap(exists -> {
                    co.com.pragma.api.dto.response.ApiResponse<Boolean> response =
                            co.com.pragma.api.dto.response.ApiResponse.<Boolean>builder()
                                    .message(UserMessages.USER_VERIFY)
                                    .code(200)
                                    .success(true)
                                    .data(exists)
                                    .build();
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                });
    }

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
    public Mono<ServerResponse> validateUserRole(ServerRequest serverRequest) {
        String identification = serverRequest.pathVariable(UserConstants.IDENTIFICATION);

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(securityContext -> {
                    var authentication = securityContext.getAuthentication();

                    if (authentication == null || !authentication.isAuthenticated()) {
                        return ServerResponse.status(401)
                                .bodyValue(ApiResponse.<Object>builder()
                                        .message(UserMessages.USER_NOT_AUTHENTICATE)
                                        .code(401)
                                        .success(false)
                                        .data(null)
                                        .build());
                    }

                    String username = authentication.getName();
                    List<String> roles = authentication.getAuthorities().stream()
                            .map(granted -> granted.getAuthority())
                            .toList();

                    if (!roles.contains("ROLE_ASESOR")) {
                        return ServerResponse.status(403)
                                .bodyValue(ApiResponse.<Object>builder()
                                        .message("Acceso denegado: no tienes el rol ASESOR")
                                        .code(403)
                                        .success(false)
                                        .data(null)
                                        .build());
                    }

                    return userUseCase.existsUserByIdentification(identification)
                            .flatMap(exists -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(ApiResponse.<Boolean>builder()
                                            .message("Usuario encontrado, rol validado")
                                            .code(200)
                                            .success(true)
                                            .data(exists)
                                            .build()))
                            .switchIfEmpty(ServerResponse.status(404)
                                    .bodyValue(ApiResponse.<Object>builder()
                                            .message("Usuario no encontrado")
                                            .code(404)
                                            .success(false)
                                            .data(null)
                                            .build()));
                })
                .switchIfEmpty(ServerResponse.status(401)
                        .bodyValue(ApiResponse.<Object>builder()
                                .message("No hay contexto de seguridad (token ausente)")
                                .code(401)
                                .success(false)
                                .data(null)
                                .build()));
    }


*/


package co.com.pragma.api;

import co.com.pragma.api.dto.request.UserRequest;
import co.com.pragma.api.mapper.UserRequestMapper;
import co.com.pragma.api.mapper.UserResponseMapper;
import co.com.pragma.api.util.ResponseBuilder;
import co.com.pragma.model.user.constants.HttpCode;
import co.com.pragma.model.user.constants.UserConstants;
import co.com.pragma.model.user.constants.UserMessages;
import co.com.pragma.model.user.constants.UserRole;
import co.com.pragma.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserHandler {

    private final UserUseCase userUseCase;
    private final UserRequestMapper userRequestMapper;
    private final UserResponseMapper userResponseMapper;

    /**
     * ✅ Verifica si un usuario existe en el sistema a partir de su identificación.
     *
     * @param serverRequest contiene la identificación del usuario en el path.
     * @return Mono con la respuesta HTTP:
     * <ul>
     *   <li>200 OK → si el usuario existe o no (boolean).</li>
     * </ul>
     */
    public Mono<ServerResponse> existsUserByIdentification(ServerRequest serverRequest) {
        String identification = serverRequest.pathVariable(UserConstants.IDENTIFICATION);
        return userUseCase.existsUserByIdentification(identification)
                .flatMap(exists -> ResponseBuilder.success(
                        exists,
                        UserMessages.USER_VERIFY,
                        HttpCode.OK.getValue()
                ));
    }

    /**
     * ✅ Crea un nuevo usuario en el sistema.
     *
     * @param serverRequest contiene el cuerpo de la petición con la información del usuario
     *                      en formato {@link UserRequest}.
     * @return Mono con la respuesta HTTP:
     * <ul>
     *   <li>201 CREATED → si el usuario fue creado exitosamente.</li>
     *   <li>400 BAD REQUEST → si ocurrió un error en la creación.</li>
     * </ul>
     */
    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserRequest.class)
                .map(userRequestMapper::toDomain)  // DTO → Domain
                .flatMap(userUseCase::saveUser)
                .map(userResponseMapper::toResponse) // Domain → DTO
                .flatMap(userResponse -> ResponseBuilder.success(
                        userResponse,
                        UserMessages.USUARIO_CREADO,
                        HttpCode.CREATED.getValue()
                ))
                .onErrorResume(e -> ResponseBuilder.error(
                        "Error: " + e.getMessage(),
                        HttpCode.BAD_REQUEST.getValue()
                ));
    }

    /**
     * ✅ Valida que el usuario autenticado tenga el rol requerido
     * y que exista en el sistema.
     *
     * @param request contiene la identificación del usuario en el path.
     * @return Mono con la respuesta HTTP:
     * <ul>
     *   <li>200 OK → si el usuario existe y cumple con el rol.</li>
     *   <li>401 UNAUTHORIZED → si no hay contexto de seguridad o no está autenticado.</li>
     *   <li>403 FORBIDDEN → si el usuario no tiene el rol requerido.</li>
     *   <li>404 NOT FOUND → si el usuario no existe en la base de datos.</li>
     * </ul>
     */
    public Mono<ServerResponse> validateUserRole(ServerRequest request) {
        String identification = request.pathVariable(UserConstants.IDENTIFICATION);

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(auth -> validateAuthentication(auth)
                        .flatMap(authentication -> validateRole(authentication, UserRole.ROLE_ASESOR)
                                .flatMap(valid -> checkUserExists(identification))
                        )
                )
                .switchIfEmpty(ResponseBuilder.error(
                        UserMessages.USER_NO_SECURITY_CONTEXT,
                        HttpCode.UNAUTHORIZED.getValue()
                ));
    }

    /**
     * ✅ Valida si la autenticación es válida (no nula y autenticada).
     *
     * @param authentication objeto de autenticación proveniente del contexto de seguridad.
     * @return Mono con la autenticación si es válida, o un error 401 en caso contrario.
     */
    private Mono<Authentication> validateAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseBuilder.error(
                    UserMessages.USER_NOT_AUTHENTICATE,
                    HttpCode.UNAUTHORIZED.getValue()
            ).then(Mono.empty());
        }
        return Mono.just(authentication);
    }

    /**
     * ✅ Verifica si el usuario autenticado tiene el rol requerido.
     *
     * @param authentication objeto de autenticación.
     * @param requiredRole   rol necesario para acceder al recurso.
     * @return Mono con {@code true} si el rol es válido,
     * o un error 403 si no cumple con el rol.
     */
    private Mono<Boolean> validateRole(Authentication authentication, UserRole requiredRole) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (!roles.contains(requiredRole.getValue())) {
            return ResponseBuilder.error(
                    UserMessages.USER_ACCESS_DENIED_ASESOR,
                    HttpCode.FORBIDDEN.getValue()
            ).then(Mono.empty());
        }
        return Mono.just(true);
    }

    /**
     * ✅ Verifica si el usuario con la identificación proporcionada existe en el sistema.
     *
     * @param identification número de identificación del usuario.
     * @return Mono con la respuesta HTTP:
     * <ul>
     *   <li>200 OK → si el usuario existe.</li>
     *   <li>404 NOT FOUND → si el usuario no fue encontrado.</li>
     * </ul>
     */
    private Mono<ServerResponse> checkUserExists(String identification) {
        return userUseCase.existsUserByIdentification(identification)
                .filter(Boolean::booleanValue) // 🔥 Solo deja pasar si es true
                .flatMap(exists -> ResponseBuilder.success(
                        true, // forzamos el true en la respuesta
                        UserMessages.USER_VALIDATED,
                        HttpCode.OK.getValue()
                ))
                .switchIfEmpty(ResponseBuilder.error(
                        UserMessages.USER_NOT_FOUND,
                        HttpCode.OK.getValue()
                ));
    }

}




