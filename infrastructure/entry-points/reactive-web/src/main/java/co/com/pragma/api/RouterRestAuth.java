package co.com.pragma.api;

import co.com.pragma.api.dto.request.LoginRequest;
import co.com.pragma.api.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRestAuth {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/login",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "login",
                    operation = @Operation(
                            operationId = "login",
                            summary = "Authenticate user and generate JWT token",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(implementation = LoginRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Successful authentication",
                                            content = @Content(schema = @Schema(implementation = LoginResponse.class))
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Invalid credentials"
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> authRoutes(AuthHandler authHandler) {
        return route()
                .POST("/api/login", authHandler::login)
                .build();
    }
}
